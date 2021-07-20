package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.util.Log
import com.nice.bluetooth.AndroidObservationEvent.CharacteristicChange
import com.nice.bluetooth.common.*
import com.nice.bluetooth.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.nice.bluetooth.gatt.Callback
import com.nice.bluetooth.gatt.GattStatus
import com.nice.bluetooth.gatt.PreferredPhy
import com.nice.bluetooth.gatt.Response
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*

private val GattSuccess = GattStatus(GATT_SUCCESS)

private val ClientCharacteristicConfigUuid = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID)

internal class Connection(
    private val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    private val onClose: () -> Unit
) : Readable, Writable {

    init {
        callback.invokeOnDisconnected(::close)
    }

    @Volatile
    private var _services: List<DiscoveredService>? = null
    val services: List<DiscoveredService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    val characteristicChanges: Flow<CharacteristicChange> = callback.onCharacteristicChanged.consumeAsFlow()
        .map { (bluetoothGattCharacteristic, value) ->
            CharacteristicChange(bluetoothGattCharacteristic.toCharacteristic(), value)
        }

    private val lock = Mutex()

    private val operationLocked = Any()
    private val transactionLocked = Any()

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
        noinline action: (suspend BluetoothGatt.() -> Unit)? = null,
        noinline actionWithResult: (suspend BluetoothGatt.() -> Boolean)? = null,
        noinline actionWithoutResult: (suspend BluetoothGatt.() -> Unit)? = null,
        noinline response: suspend () -> T
    ): T = lock.withLock(owner) {
        withContext(dispatcher) {
            if (action != null) {
                bluetoothGatt.action()
            }
            if (actionWithResult != null) {
                bluetoothGatt.execute(actionWithResult)
            }
            if (actionWithoutResult != null) {
                bluetoothGatt.tryExecute(actionWithoutResult)
            }
        }

        response()
    }

    suspend fun discoverServices() {
        execute(actionWithResult = {
            discoverServices()
        }) {
            callback.onServicesDiscovered.getOrThrow()
        }

        _services = bluetoothGatt.services.map { it.toDiscoveredService() }
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
        execute(transactionLocked, action = {
            bluetoothGatt.execute { beginReliableWrite() }
            action()
            if (!bluetoothGatt.execute { executeReliableWrite() }) {
                bluetoothGatt.tryExecute { abortReliableWrite() }
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

    suspend fun startObservation(characteristic: Characteristic) {
        val androidCharacteristic = services.getCharacteristic(characteristic)
        setCharacteristicNotification(androidCharacteristic, true)
        setConfigDescriptor(androidCharacteristic, enable = true)
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

    fun disconnect() = bluetoothGatt.disconnect()

    fun close() {
        bluetoothGatt.close()
        onClose.invoke()
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = services.getCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = services.getDescriptor(descriptor).bluetoothGattDescriptor

}

private suspend inline fun <T : Response> Channel<out T>.getOrThrow(): T {
    val response = try {
        receive()
    } catch (e: ConnectionLostException) {
        throw ConnectionLostException(cause = e)
    }
    if (response.status != GattSuccess) throw GattStatusException(response.toString())
    return response
}

private suspend inline fun BluetoothGatt.execute(crossinline action: suspend BluetoothGatt.() -> Boolean): Boolean {
    if (action()) {
        return true
    }
    throw GattRequestRejectedException()
}

private suspend inline fun BluetoothGatt.tryExecute(crossinline action: suspend BluetoothGatt.() -> Unit): Boolean {
    try {
        action()
        return true
    } catch (t: Throwable) {
        throw GattRequestRejectedException(cause = t)
    }
}

private val DiscoveredCharacteristic.configDescriptor: DiscoveredDescriptor?
    get() = findDescriptor(ClientCharacteristicConfigUuid)

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