package com.nice.bluetooth.common

import android.bluetooth.le.AdvertiseSettings

sealed class AdvertiseState {

    class Success(
        val settingsInEffect: AdvertiseSettings
    ) : AdvertiseState()

    class Failure(
        val errorCode: Int
    ) : AdvertiseState()

}