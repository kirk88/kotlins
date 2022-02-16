package com.nice.bluetooth.common

import com.nice.bluetooth.gatt.ServerResponse
import kotlinx.coroutines.flow.Flow

interface BluetoothServer {

    val advertiseState: Flow<AdvertiseState>

    val response: Flow<ServerResponse>

    fun start()

    fun stop()

}

