package com.nice.bluetooth.common

import kotlinx.coroutines.flow.Flow

interface BluetoothScanner {
    val advertisements: Flow<Advertisement>
}