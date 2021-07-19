package com.nice.bluetooth.gatt

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothProfile.*
import android.util.Log
import com.nice.bluetooth.TAG
import com.nice.bluetooth.common.ConnectionLostException
import com.nice.bluetooth.common.ConnectionState
import com.nice.bluetooth.common.Phy
import com.nice.bluetooth.external.GATT_CONN_CANCEL
import com.nice.bluetooth.external.GATT_CONN_FAIL_ESTABLISH
import com.nice.bluetooth.external.GATT_CONN_TERMINATE_PEER_USER
import com.nice.bluetooth.external.GATT_CONN_TIMEOUT
import com.nice.bluetooth.gatt.Response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.MutableStateFlow

private typealias DisconnectedAction = () -> Unit

internal data class OnCharacteristicChanged(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OnCharacteristicChanged

        if (characteristic != other.characteristic) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = characteristic.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}

internal class Callback(
    private val state: MutableStateFlow<ConnectionState>,
    private val mtu: MutableStateFlow<Int?>,
    private val phy: MutableStateFlow<PreferredPhy?>? = null
) : BluetoothGattCallback() {

    private var disconnectedAction: DisconnectedAction? = null
    fun invokeOnDisconnected(action: DisconnectedAction) {
        disconnectedAction = action
    }

    val onCharacteristicChanged = Channel<OnCharacteristicChanged>(UNLIMITED)

    val onResponse = Channel<Response>(CONFLATED)
    val onMtuChanged = Channel<OnMtuChanged>(CONFLATED)
    val onPhyUpdate = Channel<OnPhyUpdate>(CONFLATED)
    val onReliableWriteCompleted = Channel<OnReliableWriteCompleted>(CONFLATED)

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        if (newState == STATE_DISCONNECTED) {
            gatt.close()
            disconnectedAction?.invoke()
        }

        when (newState) {
            STATE_CONNECTING -> state.value = ConnectionState.Connecting
            STATE_CONNECTED -> state.value = ConnectionState.Connected
            STATE_DISCONNECTING -> state.value = ConnectionState.Disconnecting
            STATE_DISCONNECTED -> state.value = ConnectionState.Disconnected(status.toStatus())
        }

        if (newState == STATE_DISCONNECTING || newState == STATE_DISCONNECTED) {
            onCharacteristicChanged.close()
            onResponse.close(ConnectionLostException())
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        onResponse.trySendOrLog(OnServicesDiscovered(GattStatus(status)))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = characteristic.value
        onResponse.trySendOrLog(OnCharacteristicRead(characteristic, value, GattStatus(status)))
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        onResponse.trySendOrLog(OnCharacteristicWrite(characteristic, GattStatus(status)))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val event = OnCharacteristicChanged(characteristic, characteristic.value)
        onCharacteristicChanged.trySendOrLog(event)
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        onResponse.trySendOrLog(OnDescriptorRead(descriptor, descriptor.value, GattStatus(status)))
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        onResponse.trySendOrLog(OnDescriptorWrite(descriptor, GattStatus(status)))
    }

    override fun onReliableWriteCompleted(
        gatt: BluetoothGatt,
        status: Int
    ) {
        onReliableWriteCompleted.trySendOrLog(OnReliableWriteCompleted(GattStatus(status)))
    }


    override fun onMtuChanged(
        gatt: BluetoothGatt,
        mtu: Int,
        status: Int
    ) {
        onMtuChanged.trySendOrLog(OnMtuChanged(mtu, GattStatus(status)))
        if (status == GATT_SUCCESS) this.mtu.value = mtu
    }

    override fun onPhyUpdate(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
    ) {
        val preferredPhy = PreferredPhy(txPhy.toPhy(), rxPhy.toPhy())
        onPhyUpdate.trySendOrLog(OnPhyUpdate(preferredPhy, GattStatus(status)))
        if (status == GATT_SUCCESS) phy?.value = preferredPhy
    }

    override fun onPhyRead(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
    ) {
        val preferredPhy = PreferredPhy(txPhy.toPhy(), rxPhy.toPhy())
        onResponse.trySendOrLog(OnPhyRead(preferredPhy, GattStatus(status)))
    }

    override fun onReadRemoteRssi(
        gatt: BluetoothGatt,
        rssi: Int,
        status: Int
    ) {
        onResponse.trySendOrLog(OnReadRemoteRssi(rssi, GattStatus(status)))
    }

}

private fun Int.toPhy(): Phy = when (this) {
    BluetoothDevice.PHY_LE_1M -> Phy.Le1M
    BluetoothDevice.PHY_LE_2M -> Phy.Le2M
    BluetoothDevice.PHY_LE_CODED -> Phy.LeCoded
    else -> throw IllegalArgumentException("Unknown phy $this")
}

private fun Int.toStatus(): ConnectionState.Disconnected.Status? = when (this) {
    GATT_SUCCESS -> null
    GATT_CONN_TIMEOUT -> ConnectionState.Disconnected.Status.Timeout
    GATT_CONN_TERMINATE_PEER_USER -> ConnectionState.Disconnected.Status.PeripheralDisconnected
    GATT_CONN_FAIL_ESTABLISH -> ConnectionState.Disconnected.Status.Failed
    GATT_CONN_CANCEL -> ConnectionState.Disconnected.Status.Cancelled
    else -> ConnectionState.Disconnected.Status.Unknown(this)
}

private fun <E> SendChannel<E>.trySendOrLog(element: E) {
    trySend(element).getOrElse { cause ->
        Log.w(TAG, "Callback was unable to deliver $element", cause)
    }
}
