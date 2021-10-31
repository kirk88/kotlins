@file:Suppress("MissingPermission")

package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.os.Build
import android.util.Log
import com.nice.bluetooth.common.*
import com.nice.bluetooth.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.nice.bluetooth.gatt.GattStatus
import com.nice.bluetooth.gatt.Response
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.coroutines.CoroutineContext

private val STATUS_SUCCESS = GattStatus(GATT_SUCCESS)

private val CHARACTERISTIC_CONFIG_UUID = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID)

internal class PeripheralConnection(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val defaultTransport: Transport,
    private val defaultPhy: Phy,
    private val onConnected: ConnectedAction,
    private val onServicesDiscovered: ServicesDiscoveredAction
) : Readable, Writable {

    @Volatile
    private var _connectionHandler: ConnectionHandler? = null
    private val connectionHandler: ConnectionHandler
        inline get() = _connectionHandler ?: throw NotReadyException(toString())

    private val receiver = registerBluetoothStateReceiver {
        if (it == BluetoothAdapter.STATE_OFF) {
            close()
            state.value = ConnectionState.Disconnected()
        }
    }

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            receiver.unregister()
            close()
        }
    }

    private val scope = CoroutineScope(parentCoroutineContext + job)
    private val connectJob = atomic<Deferred<Throwable?>?>()

    val state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    val mtu = MutableStateFlow<Int?>(null)
    val phy = MutableStateFlow<PreferredPhy?>(null)

    @Volatile
    private var _services: List<DiscoveredService>? = null
    val services: List<DiscoveredService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    private val observers = PeripheralObservers(this)

    private fun establishConnectionHandler(autoConnect: Boolean): ConnectionHandler {
        return bluetoothDevice.connect(
            applicationContext,
            autoConnect,
            defaultTransport,
            defaultPhy,
            state,
            mtu,
            phy
        ) {
            connectJob.getAndUpdateCompat { null }?.cancel()
        } ?: throw ConnectionRejectedException()
    }

    /** Creates a connect [Job] that completes when connection is established, or failure occurs. */
    private fun connectAsync(autoConnect: Boolean) = scope.async(start = CoroutineStart.LAZY) {
        var exception: Throwable? = null

        try {
            val connectionHandler = establishConnectionHandler(autoConnect).also {
                _connectionHandler = it
            }

            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                connectionHandler.collectCharacteristicChanges(observers)
            }

            suspendUntilOrThrow<ConnectionState.Connecting.Services>()
            onConnected(AndroidConnectedPeripheral(this@PeripheralConnection))
            discoverServices()
            onServicesDiscovered(AndroidServicesDiscoveredPeripheral(this@PeripheralConnection))
            state.value = ConnectionState.Connecting.Observes
            observers.rewire()
        } catch (t: Throwable) {
            close()
            exception = t
        }

        if (exception == null) {
            state.value = ConnectionState.Connected
        }

        exception
    }

    private suspend fun discoverServices() {
        connectionHandler.execute({
            discoverServices()
        }) {
            onServicesDiscovered.receiveOrThrow()
        }

        _services = connectionHandler.services
    }

    private fun close() {
        _connectionHandler?.close()
        _connectionHandler = null
    }

    suspend fun connect(autoConnect: Boolean) {
        check(!job.isCancelled) { "Cannot connect, scope is cancelled for $this" }
        check(Bluetooth.isEnabled) { "Bluetooth is disabled" }
        connectJob.updateAndGetCompat { it ?: connectAsync(autoConnect) }!!.await()?.let {
            throw it
        }
    }

    suspend fun disconnect() {
        try {
            _connectionHandler?.apply {
                disconnect()
                suspendUntil<ConnectionState.Disconnected>()
            }
        } finally {
            close()
        }
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connectionHandler.execute({
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }) {
            onCharacteristicWrite.receiveOrThrow()
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connectionHandler.execute({
            readCharacteristic(bluetoothGattCharacteristic)
        }) {
            onCharacteristicRead.receiveOrThrow()
        }.value!!
    }

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) {
        write(bluetoothGattDescriptorFrom(descriptor), data)
    }

    private suspend fun write(
        bluetoothGattDescriptor: BluetoothGattDescriptor,
        data: ByteArray
    ) {
        connectionHandler.execute({
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }) {
            onDescriptorWrite.receiveOrThrow()
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connectionHandler.execute({
            readDescriptor(bluetoothGattDescriptor)
        }) {
            onDescriptorRead.receiveOrThrow()
        }.value!!
    }

    suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        connectionHandler.tryExecute({
            beginReliableWrite()
            var cause: Throwable? = null
            try {
                action()
            } catch (t: Throwable) {
                cause = t
                throw GattRequestRejectedException(cause = t)
            } finally {
                if (cause == null) {
                    executeReliableWrite()
                } else {
                    abortReliableWrite()
                }
            }
        }) {
            onReliableWriteCompleted.receiveOrThrow()
        }
    }

    suspend fun readRssi(): Int = connectionHandler.execute({
        readRemoteRssi()
    }) {
        onReadRemoteRssi.receiveOrThrow()
    }.rssi

    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority {
        return connectionHandler.execute({
            requestConnectionPriority(priority.intValue)
        }) {
            priority
        }
    }

    suspend fun requestMtu(mtu: Int): Int {
        return connectionHandler.execute({
            requestMtu(mtu)
        }) {
            onMtuChanged.receiveOrThrow()
        }.mtu
    }

    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionHandler.tryExecute({
                setPreferredPhy(phy.txPhy.intValue, phy.rxPhy.intValue, options.intValue)
            }) {
                onPhyUpdate.receiveOrThrow()
            }.phy
        } else {
            Log.w(
                TAG,
                "Unable to set preferred phy on a device below android8.0."
            )
            phy
        }
    }

    suspend fun readPhy(): PreferredPhy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionHandler.tryExecute({
                readPhy()
            }) {
                onPhyRead.receiveOrThrow()
            }.phy
        } else {
            Log.w(
                TAG,
                "Unable to read phy on a device below android8.0."
            )
            PreferredPhy(Phy.Le1M, Phy.Le1M)
        }
    }

    fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    suspend fun startObservation(characteristic: Characteristic) {
        val discoveredCharacteristic = services.getCharacteristic(characteristic)
        setCharacteristicNotification(discoveredCharacteristic, true)
        setConfigDescriptor(discoveredCharacteristic, enable = true)
    }

    suspend fun stopObservation(characteristic: Characteristic) {
        val discoveredCharacteristic = services.getCharacteristic(characteristic)
        try {
            setConfigDescriptor(discoveredCharacteristic, enable = false)
        } finally {
            setCharacteristicNotification(discoveredCharacteristic, false)
        }
    }

    private fun setCharacteristicNotification(
        characteristic: DiscoveredCharacteristic,
        enable: Boolean
    ) = connectionHandler.setCharacteristicNotification(
        characteristic,
        enable
    )

    private suspend fun setConfigDescriptor(
        characteristic: DiscoveredCharacteristic,
        enable: Boolean
    ) {
        val configDescriptor = characteristic.configDescriptor
        if (configDescriptor != null) {
            val bluetoothGattDescriptor = configDescriptor.bluetoothGattDescriptor
            if (enable) {
                when {
                    characteristic.supportsNotify -> write(
                        bluetoothGattDescriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                    characteristic.supportsIndicate -> write(
                        bluetoothGattDescriptor,
                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    )
                    else -> Log.w(
                        TAG,
                        "Characteristic ${characteristic.characteristicUuid} supports neither notification nor indication"
                    )
                }
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate)
                    write(
                        bluetoothGattDescriptor,
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    )
            }
        } else {
            Log.w(
                TAG,
                "Characteristic ${characteristic.characteristicUuid} is missing config descriptor."
            )
        }
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = services.getCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = services.getDescriptor(descriptor).bluetoothGattDescriptor

}

/** @throws GattStatusException if response has a non-`GATT_SUCCESS` status. */
private suspend inline fun <T : Response> Channel<out T>.receiveOrThrow(): T {
    val response = try {
        receive()
    } catch (e: ConnectionLostException) {
        throw ConnectionLostException(cause = e)
    }
    if (response.status != STATUS_SUCCESS) throw GattStatusException(response.toString())
    return response
}

private val DiscoveredCharacteristic.configDescriptor: DiscoveredDescriptor?
    get() = findDescriptor(CHARACTERISTIC_CONFIG_UUID)

private val DiscoveredCharacteristic.supportsNotify: Boolean
    get() = hasProperty(CharacteristicProperty.Notify)

private val DiscoveredCharacteristic.supportsIndicate: Boolean
    get() = hasProperty(CharacteristicProperty.Indicate)

private val PhyOptions.intValue: Int
    @TargetApi(Build.VERSION_CODES.O)
    get() = when (this) {
        PhyOptions.NoPreferred -> BluetoothDevice.PHY_OPTION_NO_PREFERRED
        PhyOptions.S2 -> BluetoothDevice.PHY_OPTION_S2
        PhyOptions.S8 -> BluetoothDevice.PHY_OPTION_S8
    }

private val WriteType.intValue: Int
    get() = when (this) {
        WriteType.WithResponse -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        WriteType.WithoutResponse -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    }

private val ConnectionPriority.intValue: Int
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() = when (this) {
        ConnectionPriority.Low -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
        ConnectionPriority.Balanced -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        ConnectionPriority.High -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    }

/**
 * Suspends until [PeripheralConnection] receiver arrives at the [ConnectionState] specified.
 *
 * @see [ConnectionState] for a description of the potential states.
 */
internal suspend inline fun <reified T : ConnectionState> PeripheralConnection.suspendUntil() {
    state.first { it is T }
}

/**
 * Suspends until [PeripheralConnection] receiver arrives at the [ConnectionState] specified or any [ConnectionState] above it.
 *
 * @see [ConnectionState] for a description of the potential states.
 * @see [ConnectionState.isAtLeast] for state ordering.
 */
internal suspend inline fun <reified T : ConnectionState> PeripheralConnection.suspendUntilAtLeast() {
    state.first { it.isAtLeast<T>() }
}

/**
 * Suspends until [PeripheralConnection] receiver arrives at the [ConnectionState] specified.
 *
 * @see ConnectionState for a description of the potential states.
 * @throws ConnectionLostException if peripheral state arrives at [ConnectionState.Disconnected].
 */
internal suspend inline fun <reified T : ConnectionState> PeripheralConnection.suspendUntilOrThrow() {
    require(T::class != ConnectionState.Disconnected::class) {
        "PeripheralConnection.suspendUntilOrThrow() throws on ConnectionState.Disconnected, not intended for use with that ConnectionState."
    }
    state
        .onEach { if (it is ConnectionState.Disconnected) throw ConnectionLostException() }
        .first { it is T }
}