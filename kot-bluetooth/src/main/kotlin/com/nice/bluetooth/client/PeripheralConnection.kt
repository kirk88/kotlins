@file:Suppress("MissingPermission")

package com.nice.bluetooth.client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.util.Log
import com.nice.bluetooth.Bluetooth
import com.nice.bluetooth.TAG
import com.nice.bluetooth.applicationContext
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
    private var _services: List<Service>? = null
    val services: List<Service>
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
            phy,
            onClose = { connectJob.getAndUpdateCompat { null }?.cancel() }
        ) ?: throw ConnectionRejectedException()
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
        connectionHandler.execute(
            action = { discoverServices() },
            response = { onServicesDiscovered.receiveOrThrow() }
        )

        _services = connectionHandler.services.map { it.toService() }
    }

    private fun close() {
        _connectionHandler?.close()
        _connectionHandler = null
    }

    suspend fun connect(autoConnect: Boolean) {
        check(Bluetooth.isEnabled) { "Bluetooth is not enabled" }
        connectJob.updateAndGetCompat { it ?: connectAsync(autoConnect) }!!.await()?.let { throw it }
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
        val bluetoothGattCharacteristic = characteristic.toBluetoothGattCharacteristic()
        connectionHandler.execute(
            action = {
                bluetoothGattCharacteristic.value = data
                bluetoothGattCharacteristic.writeType = writeType.intValue
                writeCharacteristic(bluetoothGattCharacteristic)
            },
            response = { onCharacteristicWrite.receiveOrThrow() }
        )
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = characteristic.toBluetoothGattCharacteristic()
        return connectionHandler.execute(
            action = { readCharacteristic(bluetoothGattCharacteristic) },
            response = { onCharacteristicRead.receiveOrThrow() }
        ).value!!
    }

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) {
        val bluetoothGattDescriptor = descriptor.toBluetoothGattDescriptor()
        connectionHandler.execute(
            action = {
                bluetoothGattDescriptor.value = data
                writeDescriptor(bluetoothGattDescriptor)
            },
            response = { onDescriptorWrite.receiveOrThrow() }
        )
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = descriptor.toBluetoothGattDescriptor()
        return connectionHandler.execute(
            action = { readDescriptor(bluetoothGattDescriptor) },
            response = { onDescriptorRead.receiveOrThrow() }
        ).value!!
    }

    suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        connectionHandler.tryExecute(
            action = {
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
            },
            response = { onReliableWriteCompleted.receiveOrThrow() }
        )
    }

    suspend fun readRssi(): Int {
        return connectionHandler.execute(
            action = { readRemoteRssi() },
            response = { onReadRemoteRssi.receiveOrThrow() }
        ).rssi
    }

    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority {
        return connectionHandler.execute(
            action = { requestConnectionPriority(priority.intValue) },
            response = { priority }
        )
    }

    suspend fun requestMtu(mtu: Int): Int {
        return connectionHandler.execute(
            action = { requestMtu(mtu) },
            response = { onMtuChanged.receiveOrThrow() }
        ).mtu
    }

    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionHandler.tryExecute(
                action = { setPreferredPhy(phy.txPhy.intValue, phy.rxPhy.intValue, options.intValue) },
                response = { onPhyUpdate.receiveOrThrow() }
            ).phy
        } else {
            Log.w(TAG, "Unable to set preferred phy on a device below android8.0.")
            phy
        }
    }

    suspend fun readPhy(): PreferredPhy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionHandler.tryExecute(
                action = { readPhy() },
                response = { onPhyRead.receiveOrThrow() }
            ).phy
        } else {
            Log.w(TAG, "Unable to read phy on a device below android8.0.")
            PreferredPhy(Phy.Le1M, Phy.Le1M)
        }
    }

    fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    suspend fun startObservation(characteristic: Characteristic) {
        setCharacteristicNotification(characteristic, true)
        setConfigDescriptor(characteristic, enable = true)
    }

    suspend fun stopObservation(characteristic: Characteristic) {
        try {
            setConfigDescriptor(characteristic, enable = false)
        } finally {
            setCharacteristicNotification(characteristic, false)
        }
    }

    private fun setCharacteristicNotification(
        characteristic: Characteristic,
        enable: Boolean
    ) = connectionHandler.setCharacteristicNotification(
        characteristic.toBluetoothGattCharacteristic(),
        enable
    )

    private suspend fun setConfigDescriptor(
        characteristic: Characteristic,
        enable: Boolean
    ) {
        val configDescriptor = characteristic.configDescriptor
        if (configDescriptor != null) {
            if (enable) {
                when {
                    characteristic.supportsNotify -> write(
                        configDescriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                    characteristic.supportsIndicate -> write(
                        configDescriptor,
                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    )
                    else -> Log.w(
                        TAG,
                        "Characteristic ${characteristic.uuid} supports neither notification nor indication"
                    )
                }
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate)
                    write(
                        configDescriptor,
                        BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    )
            }
        } else {
            Log.w(
                TAG,
                "Characteristic ${characteristic.uuid} is missing config descriptor."
            )
        }
    }

}

/** @throws GattStatusException if response has a non-`GATT_SUCCESS` status. */
private suspend inline fun <T : Response> Channel<out T>.receiveOrThrow(): T {
    val response = try {
        receive()
    } catch (t: Throwable) {
        throw ConnectionLostException(cause = t)
    }
    if (response.status != STATUS_SUCCESS) throw GattStatusException(response.toString())
    return response
}

private val Characteristic.configDescriptor: Descriptor?
    get() = findDescriptor(CHARACTERISTIC_CONFIG_UUID)

private val Characteristic.supportsNotify: Boolean
    get() = hasProperty(CharacteristicProperty.Notify)

private val Characteristic.supportsIndicate: Boolean
    get() = hasProperty(CharacteristicProperty.Indicate)

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