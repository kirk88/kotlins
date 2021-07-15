package com.nice.bluetooth.common

import kotlinx.coroutines.flow.Flow

interface Scanner {
    val advertisements: Flow<Advertisement>
}