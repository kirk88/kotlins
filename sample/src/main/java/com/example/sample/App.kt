package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kotlins.app.ScreenCompatAdapter
import com.nice.kotlins.app.ScreenCompatStrategy
import com.nice.kotlins.helper.isTabletDevice


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = if (isTabletDevice) ScreenCompatStrategy.BASE_ON_HEIGHT
        else ScreenCompatStrategy.BASE_ON_WIDTH

    override fun onCreate() {
        super.onCreate()
    }

}