package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kotlins.app.ScreenCompatAdapter
import com.nice.kotlins.app.ScreenCompatStrategy
import com.nice.kotlins.http.OkConfig
import com.nice.kotlins.http.OkFaker
import okhttp3.OkHttpClient


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = ScreenCompatStrategy.BASE_ON_HEIGHT

    override fun onCreate() {
        super.onCreate()

        OkFaker.setGlobalConfig(OkConfig.Builder()
            .baseUrl("https://www.baidu.com")
            .client(OkHttpClient())
            .headers(mapOf<String, String>("fdsaf")))
    }

}