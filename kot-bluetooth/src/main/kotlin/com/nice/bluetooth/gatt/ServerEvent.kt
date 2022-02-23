package com.nice.bluetooth.gatt

import com.nice.bluetooth.common.*
import com.nice.bluetooth.server.ServerHandler

sealed class ServerEvent {

    class OnConnectionStateChange(
        val handler: ServerHandler,
        val device: Device,
        val status: Int,
        val newState: Int
    ) : ServerEvent()


    class OnServiceAdded(
        val handler: ServerHandler,
        val status: Int,
        val service: Service
    ) : ServerEvent()


    class OnCharacteristicReadRequest(
        val handler: ServerHandler,
        val device: Device,
        val requestId: Int,
        val offset: Int,
        val characteristic: Characteristic
    ) : ServerEvent()

    class OnCharacteristicWriteRequest(
        val handler: ServerHandler,
        val device: Device,
        val requestId: Int,
        val characteristic: Characteristic,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val requestBytes: ByteArray
    ) : ServerEvent()


    class OnDescriptorReadRequest(
        val handler: ServerHandler,
        val device: Device,
        val requestId: Int,
        val offset: Int,
        val descriptor: Descriptor
    ) : ServerEvent()

    class OnDescriptorWriteRequest(
        val handler: ServerHandler,
        val device: Device,
        val requestId: Int,
        val descriptor: Descriptor,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val value: ByteArray
    ) : ServerEvent()

    class OnExecuteWrite(
        val handler: ServerHandler,
        val device: Device,
        val requestId: Int,
        val execute: Boolean
    ) : ServerEvent()

    class OnNotificationSent(
        val handler: ServerHandler,
        val device: Device,
        val status: Int
    ) : ServerEvent()

    class OnMtuChanged(
        val handler: ServerHandler,
        val device: Device,
        val mtu: Int
    ) : ServerEvent()

    class OnPhyRead(
        val handler: ServerHandler,
        val device: Device,
        val phy: PreferredPhy,
        val status: Int
    ) : ServerEvent()

    class OnPhyUpdate(
        val handler: ServerHandler,
        val device: Device,
        val phy: PreferredPhy,
        val status: Int
    ) : ServerEvent()

}