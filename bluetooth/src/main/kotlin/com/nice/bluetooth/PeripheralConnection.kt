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
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
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
    private var _connectionClient: ConnectionClient? = null
    private val connectionClient: ConnectionClient
        inline get() = _connectionClient ?: throw NotReadyException(toString())

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
    private val connectJob = AtomicReference<Deferred<Throwable?>?>()

    private val ready = MutableStateFlow(false)

    val state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    val mtu = MutableStateFlow<Int?>(null)
    val phy = MutableStateFlow<PreferredPhy?>(null)

    @Volatile
    private var _services: List<DiscoveredService>? = null
    val services: List<DiscoveredService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    private val observers = PeripheralObservers(this)

    suspend fun connect(autoConnect: Boolean) {
        check(!job.isCancelled) { "Cannot connect, scope is cancelled for $this" }
        check(Bluetooth.isEnabled) { "Bluetooth is disabled" }
        connectJob.updateAndGetCompat { it ?: connectAsync(autoConnect) }!!.await()?.let {
            throw it
        }
    }

    suspend fun disconnect() {
        try {
            _connectionClient?.apply {
                disconnect()
                suspendUntilDisconnected()
            }
        } finally {
            close()
        }
    }

    private fun close() {
        _connectionClient?.close()
    }

    /** Creates a connect [Job] that completes when connection is established, or failure occurs. */
    private fun connectAsync(autoConnect: Boolean) = scope.async(start = CoroutineStart.LAZY) {
        ready.value = false

        var exception: Throwable? = null

        try {
            val connectionClient = bluetoothDevice.connect(applicationContext, autoConnect, defaultTransport, defaultPhy, state, mtu, phy) {
                connectJob.getAndUpdateCompat { null }?.cancel()
            }?.also { _connectionClient = it } ?: throw ConnectionRejectedException()

            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                connectionClient.collectCharacteristicChanges(observers)
            }

            suspendUntilConnected()
            onConnected(AndroidConnectedPeripheral(this@PeripheralConnection))
            discoverServices()
            onServicesDiscovered(AndroidServicesDiscoveredPeripheral(this@PeripheralConnection))
            observers.rewire()
        } catch (t: Throwable) {
            close()
            exception = t
        }

        ready.value = true

        exception
    }

    private suspend fun discoverServices() {
        connectionClient.execute({
            discoverServices()
        }) {
            onServicesDiscovered.getOrThrow()
        }

        _services = connectionClient.services
    }

    private suspend fun suspendUntilConnected() {
        state.onEach { if (it is ConnectionState.Disconnected) throw ConnectionLostException() }
            .first { it == ConnectionState.Connected }
    }

    private suspend fun suspendUntilDisconnected() {
        state.first { it is ConnectionState.Disconnected }
    }

    suspend fun suspendUntilReady() {
        // fast path
        if (ready.value && state.value == ConnectionState.Connected) return

        // slow path
        combine(
            ready,
            state
        ) { ready, state -> ready && state == ConnectionState.Connected }.first { it }
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connectionClient.execute({
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }) {
            onCharacteristicWrite.getOrThrow()
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connectionClient.execute({
            readCharacteristic(bluetoothGattCharacteristic)
        }) {
            onCharacteristicRead.getOrThrow()
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
        connectionClient.execute({
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }) {
            onDescriptorWrite.getOrThrow()
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connectionClient.execute({
            readDescriptor(bluetoothGattDescriptor)
        }) {
            onDescriptorRead.getOrThrow()
        }.value!!
    }

    suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        connectionClient.transaction({
            try {
                beginReliableWrite()
                action()
                executeReliableWrite()
            } catch (t: Throwable) {
                abortReliableWrite()
                throw t
            }
        }) {
            onReliableWriteCompleted.getOrThrow()
        }
    }

    suspend fun readRssi(): Int = connectionClient.execute({
        readRemoteRssi()
    }) {
        onReadRemoteRssi.getOrThrow()
    }.rssi

    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectionClient.execute({
                requestConnectionPriority(priority.intValue)
            }) {
                priority
            }
        } else {
            Log.w(
                TAG,
                "Unable to request connection priority on a device below android5.0."
            )
            priority
        }
    }

    suspend fun requestMtu(mtu: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectionClient.execute({
                requestMtu(mtu)
            }) {
                onMtuChanged.getOrThrow()
            }.mtu
        } else {
            Log.w(
                TAG,
                "Unable to request mtu on a device below android5.0."
            )
            mtu
        }
    }

    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionClient.tryExecute({
                setPreferredPhy(phy.txPhy.intValue, phy.rxPhy.intValue, options.intValue)
            }) {
                onPhyUpdate.getOrThrow()
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
            connectionClient.tryExecute({
                readPhy()
            }) {
                onPhyRead.getOrThrow()
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
    ) = connectionClient.setCharacteristicNotification(
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
private suspend inline fun <T : Response> Channel<out T>.getOrThrow(): T {
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

private fun <T> AtomicReference<T>.updateAndGetCompat(operation: (T) -> T?): T? {
    var prev: T?
    var next: T?
    do {
        prev = get()
        next = operation(prev)
    } while (!compareAndSet(prev, next))
    return next
}

private fun <T> AtomicReference<T>.getAndUpdateCompat(operation: (T) -> T?): T? {
    var prev: T?
    var next: T?
    do {
        prev = get()
        next = operation(prev)
    } while (!compareAndSet(prev, next))
    return prev
}