package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.os.Build
import android.util.Log
import com.nice.bluetooth.common.*
import com.nice.bluetooth.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.nice.bluetooth.gatt.Callback
import com.nice.bluetooth.gatt.GattStatus
import com.nice.bluetooth.gatt.Response
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val bluetoothGatt: BluetoothGatt
        inline get() = connectionClient.bluetoothGatt
    private val dispatcher: CoroutineDispatcher
        inline get() = connectionClient.dispatcher
    private val callback: Callback
        inline get() = connectionClient.callback

    private val receiver = registerBluetoothStateBroadcastReceiver {
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
    private val connectJob = AtomicReference<Job?>()

    private val ready = MutableStateFlow(false)

    val state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    val mtu = MutableStateFlow<Int?>(null)
    val phy = MutableStateFlow<PreferredPhy?>(null)

    @Volatile
    private var _services: List<DiscoveredService>? = null
    val services: List<DiscoveredService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    private val observers = PeripheralObservers(this)

    private val lock = Mutex()
    private val operationLocked = Any()
    private val transactionLocked = Any()

    suspend fun connect() {
        check(!job.isCancelled) { "Cannot connect, scope is cancelled for $this" }
        check(Bluetooth.isEnabled) { "Bluetooth is disabled" }
        connectJob.updateAndGetCompat { it ?: connectAsync() }!!.join()
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
    private fun connectAsync() = scope.launch(start = CoroutineStart.LAZY) {
        ready.value = false

        _connectionClient = bluetoothDevice.connect(applicationContext, defaultTransport, defaultPhy, state, mtu, phy) {
            connectJob.getAndUpdateCompat { null }?.cancel()
        } ?: throw ConnectionRejectedException()

        try {
            suspendUntilConnected()
            onConnected(AndroidConnectedPeripheral(this@PeripheralConnection))
            discoverServices()
            onServicesDiscovered(AndroidServicesDiscoveredPeripheral(this@PeripheralConnection))
            observers.rewire()
        } catch (t: Throwable) {
            close()
            throw t
        }

        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            collectCharacteristicChanges(observers)
        }

        ready.value = true
    }

    private suspend fun collectCharacteristicChanges(observers: PeripheralObservers) {
        callback.onCharacteristicChanged.consumeAsFlow().map { (bluetoothGattCharacteristic, value) ->
            PeripheralEvent.CharacteristicChange(bluetoothGattCharacteristic.toCharacteristic(), value)
        }.collect {
            observers.send(it)
        }
    }

    private suspend fun discoverServices() {
        execute(actionWithResult = {
            discoverServices()
        }) {
            callback.onServicesDiscovered.getOrThrow()
        }

        _services = bluetoothGatt.services.map { it.toDiscoveredService() }
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

    /**
     * Executes specified [BluetoothGatt] action.
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially. In other words, the
     * response for an action must be received before another action can be performed. Additionally, the Android BLE
     * stack can be unstable if I/O isn't performed on a dedicated thread.
     *
     * These requirements are fulfilled by ensuring that all action's are performed behind a [Mutex]. On Android pre-O
     * a single threaded [CoroutineDispatcher] is used, Android O and newer a [CoroutineDispatcher] backed by an Android
     * `Handler` is used (and is also used in the Android BLE [Callback]).
     *
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    private suspend inline fun <T> execute(
        owner: Any = operationLocked,
        noinline actionWithResult: (suspend BluetoothGatt.() -> Boolean)? = null,
        noinline actionWithoutResult: (suspend BluetoothGatt.() -> Unit)? = null,
        crossinline response: suspend () -> T
    ): T = lock.withLock(owner) {
        withContext(dispatcher) {
            if (actionWithResult != null) {
                bluetoothGatt.actionWithResult() || throw GattRequestRejectedException()
            }

            if (actionWithoutResult != null) {
                try {
                    bluetoothGatt.actionWithoutResult()
                } catch (t: Throwable) {
                    throw GattRequestRejectedException(cause = t)
                }
            }
        }

        response()
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        execute(actionWithResult = {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }) {
            callback.onCharacteristicWrite.getOrThrow()
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return execute(actionWithResult = {
            readCharacteristic(bluetoothGattCharacteristic)
        }) {
            callback.onCharacteristicRead.getOrThrow()
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
        execute(actionWithResult = {
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }) {
            callback.onDescriptorWrite.getOrThrow()
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return execute(actionWithResult = {
            readDescriptor(bluetoothGattDescriptor)
        }) {
            callback.onDescriptorRead.getOrThrow()
        }.value!!
    }

    suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        execute(transactionLocked, actionWithResult = {
            try {
                bluetoothGatt.beginReliableWrite()
                action()
                bluetoothGatt.executeReliableWrite()
            } catch (t: Throwable) {
                bluetoothGatt.abortReliableWrite()
                throw t
            }
        }) {
            callback.onReliableWriteCompleted.getOrThrow()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionPriority = execute(actionWithResult = {
        requestConnectionPriority(priority.intValue)
    }) {
        priority
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestMtu(mtu: Int): Int = execute(actionWithResult = {
        requestMtu(mtu)
    }) {
        callback.onMtuChanged.getOrThrow()
    }.mtu

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy = execute(actionWithoutResult = {
        setPreferredPhy(phy.txPhy.intValue, phy.rxPhy.intValue, options.intValue)
    }) {
        callback.onPhyUpdate.getOrThrow()
    }.phy

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun readPhy(): PreferredPhy = execute(actionWithoutResult = {
        readPhy()
    }) {
        callback.onPhyRead.getOrThrow()
    }.phy

    suspend fun readRssi(): Int = execute(actionWithResult = {
        readRemoteRssi()
    }) {
        callback.onReadRemoteRssi.getOrThrow()
    }.rssi

    fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    suspend fun startObservation(characteristic: Characteristic) {
        val androidCharacteristic = services.getCharacteristic(characteristic)
        try {
            setConfigDescriptor(androidCharacteristic, enable = true)
        } finally {
            setCharacteristicNotification(androidCharacteristic, true)
        }
    }

    suspend fun stopObservation(characteristic: Characteristic) {
        val androidCharacteristic = services.getCharacteristic(characteristic)
        try {
            setConfigDescriptor(androidCharacteristic, enable = false)
        } finally {
            setCharacteristicNotification(androidCharacteristic, false)
        }
    }

    private fun setCharacteristicNotification(
        characteristic: DiscoveredCharacteristic,
        enable: Boolean
    ) = bluetoothGatt.setCharacteristicNotification(
        characteristic.bluetoothGattCharacteristic,
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