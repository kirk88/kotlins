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

enum class PhyOptions {
    NoPreferred,
    S2,
    S8
}

private val GattSuccess = GattStatus(GATT_SUCCESS)

private val ClientCharacteristicConfigUuid = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID)

internal class Connection(
    private val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    private val invokeOnClose: () -> Unit
) : Readable, Writable {

    init {
        callback.invokeOnDisconnected(::close)
    }

    @Volatile
    private var _services: List<AndroidGattService>? = null
    val services: List<AndroidGattService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    val characteristicChanges: Flow<CharacteristicChange> = callback.onCharacteristicChanged.consumeAsFlow()
        .map { (bluetoothGattCharacteristic, value) ->
            CharacteristicChange(bluetoothGattCharacteristic.toCharacteristic(), value)
        }

    private val lock = Mutex()

    private val operationLocked = Any()
    private val transactionLocked = Any()

    private suspend inline fun <T> withLock(owner: Any = operationLocked, action: () -> T) =
        lock.withLock(owner, action)

    /**
     * Executes specified [BluetoothGatt] [action].
     *
     * Android Bluetooth Low Energy has strict requirements: all I/O must be executed sequentially. In other words, the
     * response for an [action] must be received before another [action] can be performed. Additionally, the Android BLE
     * stack can be unstable if I/O isn't performed on a dedicated thread.
     *
     * These requirements are fulfilled by ensuring that all [action]s are performed behind a [Mutex]. On Android pre-O
     * a single threaded [CoroutineDispatcher] is used, Android O and newer a [CoroutineDispatcher] backed by an Android
     * `Handler` is used (and is also used in the Android BLE [Callback]).
     *
     * @throws GattRequestRejectedException if underlying `BluetoothGatt` method call returns `false`.
     * @throws GattStatusException if response has a non-`GATT_SUCCESS` status.
     */
    private suspend inline fun <reified T> execute(
        noinline response: suspend () -> T,
        crossinline action: BluetoothGatt.() -> Boolean
    ): T = withLock {
        withContext(dispatcher) {
            bluetoothGatt.execute(action)
        }

        response()
    }

    private suspend inline fun <reified T> tryExecute(
        noinline response: suspend () -> T,
        crossinline action: BluetoothGatt.() -> Unit
    ): T = withLock {
        withContext(dispatcher) {
            bluetoothGatt.tryExecute(action)
        }

        response()
    }

    suspend fun discoverServices() {
        execute(response = {
            callback.onServicesDiscovered.getOrThrow()
        }) {
            discoverServices()
        }

        _services = bluetoothGatt.services.map { it.toAndroidGattService() }
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        execute(response = {
            callback.onCharacteristicWrite.getOrThrow()
        }) {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return execute(response = {
            callback.onCharacteristicRead.getOrThrow()
        }) {
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
        execute(response = {
            callback.onDescriptorWrite.getOrThrow()
        }) {
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return execute(response = {
            callback.onDescriptorRead.getOrThrow()
        }) {
            readDescriptor(bluetoothGattDescriptor)
        }.value!!
    }

    suspend fun reliableWrite(action: suspend Writable.() -> Unit) {
        withLock(transactionLocked) {
            withContext(dispatcher) {
                bluetoothGatt.execute { beginReliableWrite() }
                action()
                if (!bluetoothGatt.execute { executeReliableWrite() }) {
                    bluetoothGatt.tryExecute { abortReliableWrite() }
                }
            }
            callback.onReliableWriteCompleted.getOrThrow()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestConnectionPriority(priority: Priority): Priority = execute(response = {
        priority
    }) {
        requestConnectionPriority(priority.intValue)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestMtu(mtu: Int): Int = execute(response = {
        callback.onMtuChanged.getOrThrow()
    }) {
        requestMtu(mtu)
    }.mtu

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun setPreferredPhy(phy: PreferredPhy, options: PhyOptions): PreferredPhy = tryExecute(response = {
        callback.onPhyUpdate.getOrThrow()
    }) {
        setPreferredPhy(phy.txPhy.intValue, phy.rxPhy.intValue, options.intValue)
    }.phy

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun readPhy(): PreferredPhy = tryExecute(response = {
        callback.onPhyRead.getOrThrow()
    }) {
        readPhy()
    }.phy

    suspend fun readRssi(): Int = execute(response = {
        callback.onReadRemoteRssi.getOrThrow()
    }) {
        readRemoteRssi()
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
        characteristic: AndroidGattCharacteristic,
        enable: Boolean
    ) = bluetoothGatt.setCharacteristicNotification(
        characteristic.bluetoothGattCharacteristic,
        enable
    )

    private suspend fun setConfigDescriptor(
        characteristic: AndroidGattCharacteristic,
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
        invokeOnClose.invoke()
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = services.getCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = services.getDescriptor(descriptor).bluetoothGattDescriptor

}

private suspend inline fun <reified T : Response> Channel<out T>.getOrThrow(): T {
    val response = try {
        receive()
    } catch (e: ConnectionLostException) {
        throw ConnectionLostException(cause = e)
    }
    if (response.status != GattSuccess) throw GattStatusException(response.toString())
    return response
}

private inline fun BluetoothGatt.execute(crossinline action: BluetoothGatt.() -> Boolean): Boolean {
    if (action()) {
        return true
    }
    throw GattRequestRejectedException()
}

private inline fun BluetoothGatt.tryExecute(crossinline action: BluetoothGatt.() -> Unit): Boolean {
    try {
        action()
        return true
    } catch (t: Throwable) {
        throw GattRequestRejectedException(cause = t)
    }
}

private val AndroidGattCharacteristic.configDescriptor: AndroidGattDescriptor?
    get() = descriptors.find { it.descriptorUuid == ClientCharacteristicConfigUuid }

private val AndroidGattCharacteristic.supportsNotify: Boolean
    get() = bluetoothGattCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0

private val AndroidGattCharacteristic.supportsIndicate: Boolean
    get() = bluetoothGattCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0

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

private val Priority.intValue: Int
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() = when (this) {
        Priority.Low -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
        Priority.Balanced -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        Priority.High -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    }