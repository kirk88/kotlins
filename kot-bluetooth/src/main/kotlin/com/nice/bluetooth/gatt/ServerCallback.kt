package com.nice.bluetooth.gatt

import android.bluetooth.*
import kotlinx.coroutines.channels.SendChannel

class ServerCallback(private val serverEvent: SendChannel<ServerEvent>): BluetoothGattServerCallback() {

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {

    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService) {
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        requestBytes: ByteArray?
    ) {
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
    }

    override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
    }

    override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
    }

}