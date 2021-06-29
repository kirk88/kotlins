package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kotlins.app.ScreenCompatAdapter
import com.nice.kotlins.app.ScreenCompatStrategy


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = ScreenCompatStrategy.BASE_ON_HEIGHT

    override fun onCreate() {
        super.onCreate()

    }

}