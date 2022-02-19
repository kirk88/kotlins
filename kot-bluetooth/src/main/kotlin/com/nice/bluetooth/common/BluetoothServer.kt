package com.nice.bluetooth.common

import com.nice.bluetooth.gatt.ServerEvent
import kotlinx.coroutines.flow.Flow

interface BluetoothServer {

    val advertiseState: Flow<AdvertiseState>

    val serverEvent: Flow<ServerEvent>

    fun addService(service: Service)

    fun start()

    fun stop()

}

