package com.nice.bluetooth.gatt

import android.bluetooth.*
import com.nice.bluetooth.common.*
import com.nice.bluetooth.server.DefaultServerHandler
import com.nice.bluetooth.server.ServerHandler
import com.nice.bluetooth.server.toDevice
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

class ServerCallback(
    private val serverEvent: SendChannel<ServerEvent>,
    serverPromise: () -> BluetoothGattServer
) : BluetoothGattServerCallback() {

    private val handler: ServerHandler by lazy {
        DefaultServerHandler(serverPromise.invoke())
    }

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        serverEvent.trySendBlocking(
            ServerEvent.OnConnectionStateChange(
                handler = handler,
                device = device.toDevice(),
                status = status,
                newState = newState
            )
        )
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService) {
        serverEvent.trySendBlocking(
            ServerEvent.OnServiceAdded(
                handler = handler,
                status = status,
                service = service.toService()
            )
        )
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        serverEvent.trySendBlocking(
            ServerEvent.OnCharacteristicReadRequest(
                handler = handler,
                device = device.toDevice(),
                requestId = requestId,
                offset = offset,
                characteristic = characteristic.toCharacteristic()
            )
        )
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        requestBytes: ByteArray
    ) {
        serverEvent.trySendBlocking(
            ServerEvent.OnCharacteristicWriteRequest(
                handler = handler,
                device = device.toDevice(),
                requestId = requestId,
                characteristic = characteristic.toCharacteristic(),
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                requestBytes = requestBytes
            )
        )
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
        serverEvent.trySendBlocking(
            ServerEvent.OnDescriptorReadRequest(
                handler = handler,
                device = device.toDevice(),
                requestId = requestId,
                offset = offset,
                descriptor = descriptor.toDescriptor()
            )
        )
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        serverEvent.trySendBlocking(
            ServerEvent.OnDescriptorWriteRequest(
                handler = handler,
                device = device.toDevice(),
                requestId = requestId,
                descriptor = descriptor.toDescriptor(),
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        )
    }

    override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
        serverEvent.trySendBlocking(
            ServerEvent.OnExecuteWrite(
                handler = handler,
                device = device.toDevice(),
                requestId = requestId,
                execute = execute
            )
        )
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        serverEvent.trySendBlocking(
            ServerEvent.OnNotificationSent(
                handler = handler,
                device = device.toDevice(),
                status = status
            )
        )
    }

    override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
        serverEvent.trySendBlocking(
            ServerEvent.OnMtuChanged(
                handler = handler,
                device = device.toDevice(),
                mtu = mtu
            )
        )
    }

    override fun onPhyRead(device: BluetoothDevice, txPhy: Int, rxPhy: Int, status: Int) {
        serverEvent.trySendBlocking(
            ServerEvent.OnPhyRead(
                handler = handler,
                device = device.toDevice(),
                phy = PreferredPhy(txPhy.phy, rxPhy.phy),
                status = status
            )
        )
    }

    override fun onPhyUpdate(device: BluetoothDevice, txPhy: Int, rxPhy: Int, status: Int) {
        serverEvent.trySendBlocking(
            ServerEvent.OnPhyUpdate(
                handler = handler,
                device = device.toDevice(),
                phy = PreferredPhy(txPhy.phy, rxPhy.phy),
                status = status
            )
        )
    }

}