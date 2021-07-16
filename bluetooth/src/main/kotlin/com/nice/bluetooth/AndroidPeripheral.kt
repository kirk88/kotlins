package com.nice.bluetooth

import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor.*
import android.util.Log
import com.nice.bluetooth.common.*
import com.nice.bluetooth.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.nice.bluetooth.gatt.PreferredPhy
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
    builderAction: PeripheralBuilderAction = {}
): Peripheral = peripheral(advertisement.device, builderAction)

fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {}
): Peripheral {
    val builder = AndroidPeripheralBuilder()
    builder.builderAction()
    return AndroidPeripheral(
        coroutineContext,
        bluetoothDevice,
        builder.transport,
        builder.phy,
        builder.onServicesDiscovered
    )
}

class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val defaultTransport: Transport,
    private val defaultPhy: Phy,
    private val onServicesDiscovered: ServicesDiscoveredAction
) : Peripheral {

    private val receiver = registerBluetoothStateBroadcastReceiver { state ->
        if (state == STATE_OFF) {
            closeConnection()
            _state.value = ConnectionState.Disconnected()
        }
    }

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            applicationContext.unregisterReceiver(receiver)
            closeConnection()
        }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    override val state: Flow<ConnectionState> = _state.asStateFlow()

    private val _mtu = MutableStateFlow<Int?>(null)
    override val mtu: Flow<Int?> = _mtu.asStateFlow()

    private val _phy = MutableStateFlow<PreferredPhy?>(null)
    override val phy: Flow<PreferredPhy?> = _phy.asStateFlow()

    private val observers = Observers(this)

    private val androidGattServices: List<AndroidGattService>
        get() = connection.services
    override val services: List<DiscoveredService>
        get() = androidGattServices.map { it.toDiscoveredService() }

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    private val connectJob = atomic<Deferred<Unit>?>(null)

    private val ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        // fast path
        if (ready.value && _state.value == ConnectionState.Connected) return

        // slow path
        combine(ready, state) { ready, state -> ready && state == ConnectionState.Connected }.first { it }
    }

    private fun establishConnection(): Connection =
        bluetoothDevice.connect(
            applicationContext,
            defaultTransport,
            defaultPhy,
            _state,
            _mtu,
            _phy
        ) { connectJob.value = null } ?: throw ConnectionRejectedException()

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
            connection.discoverServices()
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
                disconnect()
                suspendUntilDisconnected()
            }
        } finally {
            closeConnection()
        }
    }

    override suspend fun rssi(): Int = connection.rssi()

    override suspend fun requestConnectionPriority(priority: Priority): Boolean =
        connection.requestConnectionPriority(priority)

    override suspend fun requestMtu(mtu: Int): Boolean = connection.requestMtu(mtu)

    override suspend fun readPhy(): Boolean = connection.readPhy()

    override suspend fun setPreferredPhy(txPhy: Phy, rxPhy: Phy, options: PhyOptions): Boolean {
        return connection.setPreferredPhy(txPhy, rxPhy, options)
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) = connection.write(characteristic, data, writeType)

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = connection.read(characteristic)

    override suspend fun write(
        descriptor: DiscoveredDescriptor,
        data: ByteArray
    ) = connection.write(descriptor, data)

    override suspend fun read(
        descriptor: DiscoveredDescriptor
    ): ByteArray = connection.read(descriptor)

    override suspend fun reliableWrite(operation: suspend Writable.() -> Unit) {
        connection.reliableWrite(operation)
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    internal suspend fun startObservation(characteristic: Characteristic) {
        val platformCharacteristic = androidGattServices.findCharacteristic(characteristic)
        connection.setCharacteristicNotification(platformCharacteristic, true)
        setConfigDescriptor(platformCharacteristic, enable = true)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        val platformCharacteristic = androidGattServices.findCharacteristic(characteristic)

        try {
            setConfigDescriptor(platformCharacteristic, enable = false)
        } finally {
            connection.setCharacteristicNotification(platformCharacteristic, false)
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
                    characteristic.supportsNotify -> connection.write(
                        bluetoothGattDescriptor,
                        ENABLE_NOTIFICATION_VALUE
                    )
                    characteristic.supportsIndicate -> connection.write(
                        bluetoothGattDescriptor,
                        ENABLE_INDICATION_VALUE
                    )
                    else -> Log.w(
                        TAG,
                        "Characteristic ${characteristic.characteristicUuid} supports neither notification nor indication"
                    )
                }
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate)
                    connection.write(bluetoothGattDescriptor, DISABLE_NOTIFICATION_VALUE)
            }
        } else {
            Log.w(
                TAG,
                "Characteristic ${characteristic.characteristicUuid} is missing config descriptor."
            )
        }
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private suspend fun Peripheral.suspendUntilConnected() {
    state.onEach { if (it is ConnectionState.Disconnected) throw ConnectionLostException() }
        .first { it == ConnectionState.Connected }
}

private suspend fun Peripheral.suspendUntilDisconnected() {
    state.first { it is ConnectionState.Disconnected }
}

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

    val actual = getDefaultAdapter().state
    if (expected != actual) {
        val actualName = nameFor(actual)
        val expectedName = nameFor(expected)
        throw BluetoothDisabledException("Bluetooth adapter state is $actualName ($actual), but $expectedName ($expected) was required.")
    }
}
