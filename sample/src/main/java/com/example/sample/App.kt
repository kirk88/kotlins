package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.common.app.ScreenCompatAdapter
import com.nice.common.app.ScreenCompatStrategy
import com.nice.common.helper.isTabletDevice


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = if (isTabletDevice) ScreenCompatStrategy.BASE_ON_HEIGHT
        else ScreenCompatStrategy.BASE_ON_WIDTH

    override fun onCreate() {
        super.onCreate()
    }

}