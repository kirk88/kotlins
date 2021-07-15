package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.*
import android.util.Log
import com.juul.kable.launchIn
import com.nice.bluetooth.common.*
import com.nice.bluetooth.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.nice.bluetooth.gatt.Response.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.coroutines.CoroutineContext

private val clientCharacteristicConfigUuid = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID)

fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction
): Peripheral = peripheral(advertisement.device, builderAction)

fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {}
): Peripheral {
    val builder = AndroidPeripheralBuilder()
    builder.builderAction()
    return AndroidPeripheral(coroutineContext, bluetoothDevice, builder.transport, builder.phy, builder.onServicesDiscovered)
}

enum class Priority { Low, Balanced, High }

class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val transport: Transport,
    private val phy: Phy,
    private val onServicesDiscovered: ServicesDiscoveredAction
) : Peripheral {

    private val receiver = registerBluetoothStateBroadcastReceiver { state ->
        if (state == STATE_OFF) {
            closeConnection()
            _state.value = State.Disconnected()
        }
    }

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            applicationContext.unregisterReceiver(receiver)
            closeConnection()
        }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State>(State.Disconnected())
    override val state: Flow<State> = _state.asStateFlow()

    private val _mtu = MutableStateFlow<Int?>(null)

    /**
     * [StateFlow] of the most recently negotiated MTU. The MTU will change upon a successful request to change the MTU
     * (via [requestMtu]), or if the peripheral initiates an MTU change. [StateFlow]'s `value` will be `null` until MTU
     * is negotiated.
     */
    val mtu: StateFlow<Int?> = _mtu.asStateFlow()

    private val observers = Observers(this)

    @Volatile
    private var _androidGattServices: List<AndroidGattService>? = null
    private val androidGattServices: List<AndroidGattService>
        get() = checkNotNull(_androidGattServices) { "Services have not been discovered for $this" }

    override val services: List<DiscoveredService>?
        get() = _androidGattServices?.map { it.toDiscoveredService() }

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    private val connectJob = atomic<Deferred<Unit>?>(null)

    private val ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        // fast path
        if (ready.value && _state.value == State.Connected) return

        // slow path
        combine(ready, state) { ready, state -> ready && state == State.Connected }.first { it }
    }

    private fun establishConnection(): Connection =
        bluetoothDevice.connect(
            applicationContext,
            transport,
            phy,
            _state,
            _mtu,
            invokeOnClose = { connectJob.value = null }
        ) ?: throw ConnectionRejectedException()

    /** Creates a connect [Job] that completes when connection is established, or failure occurs. */
    private fun connectAsync() = scope.async(start = LAZY) {
        ready.value = false

        val connection = establishConnection().also { _connection = it }
        connection
            .characteristicChanges
            .onEach(observers.characteristicChanges::emit)
            .launchIn(scope, UNDISPATCHED)

        try {
            suspendUntilConnected()
            discoverServices()
            onServicesDiscovered(AndroidServicesDiscoveredPeripheral(this@AndroidPeripheral))
            observers.rewire()
        } catch (t: Throwable) {
            closeConnection()
            throw t
        }

        ready.value = true
    }

    private fun closeConnection() {
        _connection?.close()
        _connection = null
    }

    override suspend fun connect() {
        check(job.isNotCancelled) { "Cannot connect, scope is cancelled for $this" }
        checkBluetoothAdapterState(expected = STATE_ON)
        connectJob.updateAndGet { it ?: connectAsync() }!!.await()
    }

    override suspend fun disconnect() {
        try {
            _connection?.apply {
                bluetoothGatt.disconnect()
                suspendUntilDisconnected()
            }
        } finally {
            closeConnection()
        }
    }

    fun requestConnectionPriority(priority: Priority): Boolean =
        connection.bluetoothGatt.requestConnectionPriority(priority.intValue)

    override suspend fun rssi(): Int = connection.execute<OnReadRemoteRssi> {
        readRemoteRssi()
    }.rssi

    private suspend fun discoverServices() {
        connection.execute<OnServicesDiscovered> {
            discoverServices()
        }
        _androidGattServices = connection.bluetoothGatt
            .services
            .map { it.toAndroidGattService() }
    }

    /**
     * Requests that the current connection's MTU be changed. Suspends until the MTU changes, or failure occurs. The
     * negotiated MTU value is returned, which may not be [mtu] value requested if the remote peripheral negotiated an
     * alternate MTU.
     *
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     * @throws GattRequestRejectedException if Android was unable to fulfill the MTU change request.
     * @throws GattStatusException if MTU change request failed.
     */
    suspend fun requestMtu(mtu: Int): Int = connection.requestMtu(mtu)

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.execute<OnCharacteristicWrite> {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connection.execute<OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
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
        connection.execute<OnDescriptorWrite> {
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connection.execute<OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value!!
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    internal suspend fun startObservation(characteristic: Characteristic) {
        val platformCharacteristic = androidGattServices.findCharacteristic(characteristic)
        connection
            .bluetoothGatt
            .setCharacteristicNotification(platformCharacteristic, true)
        setConfigDescriptor(platformCharacteristic, enable = true)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        val platformCharacteristic = androidGattServices.findCharacteristic(characteristic)

        try {
            setConfigDescriptor(platformCharacteristic, enable = false)
        } finally {
            connection
                .bluetoothGatt
                .setCharacteristicNotification(platformCharacteristic, false)
        }
    }

    private suspend fun setConfigDescriptor(
        characteristic: AndroidCharacteristic,
        enable: Boolean
    ) {
        val configDescriptor = characteristic.configDescriptor
        if (configDescriptor != null) {
            val bluetoothGattDescriptor = configDescriptor.bluetoothGattDescriptor

            if (enable) {
                when {
                    characteristic.supportsNotify -> write(bluetoothGattDescriptor, ENABLE_NOTIFICATION_VALUE)
                    characteristic.supportsIndicate -> write(bluetoothGattDescriptor, ENABLE_INDICATION_VALUE)
                    else -> Log.w(TAG, "Characteristic ${characteristic.characteristicUuid} supports neither notification nor indication")
                }
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate)
                    write(bluetoothGattDescriptor, DISABLE_NOTIFICATION_VALUE)
            }
        } else {
            Log.w(TAG, "Characteristic ${characteristic.characteristicUuid} is missing config descriptor.")
        }
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = androidGattServices.findCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = androidGattServices.findDescriptor(descriptor).bluetoothGattDescriptor

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private suspend fun Peripheral.suspendUntilConnected() {
    state
        .onEach { if (it is State.Disconnected) throw ConnectionLostException() }
        .first { it == State.Connected }
}

private suspend fun Peripheral.suspendUntilDisconnected() {
    state.first { it is State.Disconnected }
}

private val WriteType.intValue: Int
    get() = when (this) {
        WriteType.WithResponse -> WRITE_TYPE_DEFAULT
        WriteType.WithoutResponse -> WRITE_TYPE_NO_RESPONSE
    }

private val Priority.intValue: Int
    get() = when (this) {
        Priority.Low -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
        Priority.Balanced -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        Priority.High -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    }

private fun BluetoothGatt.setCharacteristicNotification(
    characteristic: AndroidCharacteristic,
    enable: Boolean
) = setCharacteristicNotification(characteristic.bluetoothGattCharacteristic, enable)

private val AndroidCharacteristic.configDescriptor: AndroidDescriptor?
    get() = descriptors.firstOrNull(clientCharacteristicConfigUuid)

private val AndroidCharacteristic.supportsNotify: Boolean
    get() = bluetoothGattCharacteristic.properties and PROPERTY_NOTIFY != 0

private val AndroidCharacteristic.supportsIndicate: Boolean
    get() = bluetoothGattCharacteristic.properties and PROPERTY_INDICATE != 0

/**
 * Explicitly check the adapter state before connecting in order to respect system settings.
 * Android doesn't actually turn bluetooth off when the setting is disabled, so without this
 * check we're able to reconnect the device illegally.
 */
private fun checkBluetoothAdapterState(
    expected: Int
) {
    fun nameFor(value: Int) = when (value) {
        STATE_OFF -> "Off"
        STATE_ON -> "On"
        STATE_TURNING_OFF -> "TurningOff"
        STATE_TURNING_ON -> "TurningOn"
        else -> "Unknown"
    }

    val actual = BluetoothAdapter.getDefaultAdapter().state
    if (expected != actual) {
        val actualName = nameFor(actual)
        val expectedName = nameFor(expected)
        throw BluetoothDisabledException("Bluetooth adapter state is $actualName ($actual), but $expectedName ($expected) was required.")
    }
}
