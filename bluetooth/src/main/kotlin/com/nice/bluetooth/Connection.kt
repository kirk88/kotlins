package com.nice.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import com.nice.bluetooth.AndroidObservationEvent.CharacteristicChange
import com.nice.bluetooth.common.*
import com.nice.bluetooth.gatt.Callback
import com.nice.bluetooth.gatt.GattStatus
import com.nice.bluetooth.gatt.Response
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class OutOfOrderGattCallbackException internal constructor(
    message: String
) : IllegalStateException(message)

private val GattSuccess = GattStatus(GATT_SUCCESS)

enum class PhyOptions {
    NoPreferred,
    S2,
    S8
}

internal class Connection(
    private val bluetoothGatt: BluetoothGatt,
    private val dispatcher: CoroutineDispatcher,
    private val callback: Callback,
    private val invokeOnClose: () -> Unit
) : Readable, Writable {

    init {
        callback.invokeOnDisconnected(::close)
    }

    val characteristicChanges: Flow<CharacteristicChange> = callback.onCharacteristicChanged
        .map { (bluetoothGattCharacteristic, value) ->
            CharacteristicChange(bluetoothGattCharacteristic.toLazyCharacteristic(), value)
        }

    @Volatile
    private var _services: List<AndroidGattService>? = null
    val services: List<AndroidGattService>
        get() = checkNotNull(_services) { "Services have not been discovered for $this" }

    private val lock = Mutex()

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
        crossinline action: BluetoothGatt.() -> Boolean
    ): T = lock.withLock {
        withContext(dispatcher) {
            action.invoke(bluetoothGatt) || throw GattRequestRejectedException()
        }

        val response = try {
            callback.onResponse.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        if (response.status != GattSuccess) throw GattStatusException(response.toString())

        // `lock` should always enforce a 1:1 matching of request to response, but if an Android `BluetoothGattCallback`
        // method gets called out of order then we'll cast to the wrong response type.
        response as? T
            ?: throw OutOfOrderGattCallbackException(
                "Unexpected response type ${response.javaClass.simpleName} received"
            )
    }

    suspend fun rssi(): Int = execute<Response.OnReadRemoteRssi> {
        readRemoteRssi()
    }.rssi

    suspend fun discoverServices() {
        execute<Response.OnServicesDiscovered> {
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
        execute<Response.OnCharacteristicWrite> {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    override suspend fun read(
        characteristic: Characteristic
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return execute<Response.OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
        }.value!!
    }

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) {
        write(bluetoothGattDescriptorFrom(descriptor), data)
    }

    suspend fun write(
        bluetoothGattDescriptor: BluetoothGattDescriptor,
        data: ByteArray
    ) {
        execute<Response.OnDescriptorWrite> {
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    override suspend fun read(
        descriptor: Descriptor
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return execute<Response.OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value!!
    }

    suspend fun reliableWrite(operation: suspend Writable.() -> Unit) {
        try {
            bluetoothGatt.beginReliableWrite()
            operation()
            bluetoothGatt.executeReliableWrite()
        } catch (t: Throwable) {
            bluetoothGatt.abortReliableWrite()
            throw t
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestMtu(mtu: Int): Boolean = lock.withLock {
        withContext(dispatcher) {
            bluetoothGatt.requestMtu(mtu)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun requestConnectionPriority(priority: Priority): Boolean = lock.withLock {
        withContext(dispatcher) {
            bluetoothGatt.requestConnectionPriority(priority.intValue)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun readPhy(): Boolean = lock.withLock {
        withContext(dispatcher) {
            bluetoothGatt.readPhy()
        }

        val response = try {
            callback.onPhyRead.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        response.status == GattSuccess
    }

    @TargetApi(Build.VERSION_CODES.O)
    suspend fun setPreferredPhy(txPhy: Phy, rxPhy: Phy, options: PhyOptions): Boolean = lock.withLock {
        withContext(dispatcher) {
            bluetoothGatt.setPreferredPhy(txPhy.intValue, rxPhy.intValue, options.intValue)
        }

        val response = try {
            callback.onPhyUpdate.receive()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }

        response.status == GattSuccess
    }

    fun setCharacteristicNotification(
        characteristic: AndroidCharacteristic,
        enable: Boolean
    ) = bluetoothGatt.setCharacteristicNotification(
        characteristic.bluetoothGattCharacteristic,
        enable
    )

    fun disconnect() = bluetoothGatt.disconnect()

    fun close() {
        bluetoothGatt.close()
        invokeOnClose.invoke()
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = services.findCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = services.findDescriptor(descriptor).bluetoothGattDescriptor

}

private val PhyOptions.intValue: Int
    @TargetApi(Build.VERSION_CODES.O)
    get() = when(this){
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