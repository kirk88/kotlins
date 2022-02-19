package com.nice.bluetooth.gatt

import com.nice.bluetooth.common.Device

sealed class ServerEvent {

    class OnConnectionStateChange(
        val device: Device,
        val status: Int,
        val newState: Int
    ) : ServerEvent()

}