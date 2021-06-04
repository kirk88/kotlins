package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kotlins.app.ScreenCompatAdapter


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val baseScreenWidth: Int = 600

    override fun onCreate() {
        super.onCreate()
    }

}