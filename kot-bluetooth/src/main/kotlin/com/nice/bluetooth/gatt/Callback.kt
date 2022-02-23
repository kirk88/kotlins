@file:Suppress("MissingPermission")

package com.nice.bluetooth.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile.*
import android.util.Log
import com.nice.bluetooth.TAG
import com.nice.bluetooth.common.ConnectionLostException
import com.nice.bluetooth.common.ConnectionState
import com.nice.bluetooth.common.PreferredPhy
import com.nice.bluetooth.common.phy
import com.nice.bluetooth.external.*
import com.nice.bluetooth.gatt.Response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.MutableStateFlow

private typealias DisconnectedAction = () -> Unit

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
    val onServicesDiscovered = Channel<OnServicesDiscovered>(CONFLATED)
    val onCharacteristicRead = Channel<OnCharacteristicRead>(CONFLATED)
    val onCharacteristicWrite = Channel<OnCharacteristicWrite>(CONFLATED)
    val onDescriptorRead = Channel<OnDescriptorRead>(CONFLATED)
    val onDescriptorWrite = Channel<OnDescriptorWrite>(CONFLATED)
    val onMtuChanged = Channel<OnMtuChanged>(CONFLATED)
    val onPhyUpdate = Channel<OnPhyUpdate>(CONFLATED)
    val onPhyRead = Channel<OnPhyRead>(CONFLATED)
    val onReadRemoteRssi = Channel<OnReadRemoteRssi>(CONFLATED)
    val onReliableWriteCompleted = Channel<OnReliableWriteCompleted>(CONFLATED)

    private fun close() {
        val cause = ConnectionLostException()
        onCharacteristicChanged.close()
        onServicesDiscovered.close(cause)
        onCharacteristicRead.close(cause)
        onCharacteristicWrite.close(cause)
        onDescriptorRead.close(cause)
        onDescriptorWrite.close(cause)
        onMtuChanged.close(cause)
        onPhyUpdate.close(cause)
        onPhyRead.close(cause)
        onReadRemoteRssi.close(cause)
        onReliableWriteCompleted.close(cause)

        disconnectedAction?.invoke()
    }

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        when (newState) {
            STATE_CONNECTING -> state.value = ConnectionState.Connecting.Device
            STATE_CONNECTED -> state.value = ConnectionState.Connecting.Services
            STATE_DISCONNECTING -> state.value = ConnectionState.Disconnecting
            STATE_DISCONNECTED -> state.value = ConnectionState.Disconnected(status.connectionState)
        }

        if (newState == STATE_DISCONNECTED) {
            gatt.close()
            close()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        onServicesDiscovered.trySendOrLog(OnServicesDiscovered(GattStatus(status)))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = characteristic.value
        onCharacteristicRead.trySendOrLog(
            OnCharacteristicRead(
                characteristic,
                value,
                GattStatus(status)
            )
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        onCharacteristicWrite.trySendOrLog(
            OnCharacteristicWrite(
                characteristic,
                GattStatus(status)
            )
        )
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
        onDescriptorRead.trySendOrLog(
            OnDescriptorRead(
                descriptor,
                descriptor.value,
                GattStatus(status)
            )
        )
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        onDescriptorWrite.trySendOrLog(OnDescriptorWrite(descriptor, GattStatus(status)))
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
        val preferredPhy = PreferredPhy(txPhy.phy, rxPhy.phy)
        onPhyUpdate.trySendOrLog(OnPhyUpdate(preferredPhy, GattStatus(status)))
        if (status == GATT_SUCCESS) phy?.value = preferredPhy
    }

    override fun onPhyRead(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
    ) {
        val preferredPhy = PreferredPhy(txPhy.phy, rxPhy.phy)
        onPhyRead.trySendOrLog(OnPhyRead(preferredPhy, GattStatus(status)))
    }

    override fun onReadRemoteRssi(
        gatt: BluetoothGatt,
        rssi: Int,
        status: Int
    ) {
        onReadRemoteRssi.trySendOrLog(OnReadRemoteRssi(rssi, GattStatus(status)))
    }

}

private val Int.connectionState: ConnectionState.Disconnected.Status?
    get() = when (this) {
        GATT_SUCCESS -> null
        GATT_CONN_L2C_FAILURE -> ConnectionState.Disconnected.Status.L2CapFailure
        GATT_CONN_TIMEOUT -> ConnectionState.Disconnected.Status.Timeout
        GATT_CONN_TERMINATE_PEER_USER -> ConnectionState.Disconnected.Status.PeripheralDisconnected
        GATT_CONN_TERMINATE_LOCAL_HOST -> ConnectionState.Disconnected.Status.CentralDisconnected
        GATT_CONN_FAIL_ESTABLISH -> ConnectionState.Disconnected.Status.Failed
        GATT_CONN_LMP_TIMEOUT -> ConnectionState.Disconnected.Status.LinkManagerProtocolTimeout
        GATT_CONN_CANCEL -> ConnectionState.Disconnected.Status.Cancelled
        else -> ConnectionState.Disconnected.Status.Unknown(this)
    }

private fun <E> SendChannel<E>.trySendOrLog(element: E) {
    trySend(element).onFailure { cause ->
        Log.w(TAG, "Callback was unable to deliver $element", cause)
    }
}
