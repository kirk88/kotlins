package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kothttp.OkHttpConfig
import okhttp3.OkHttpClient


class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        OkHttpConfig.Setter()
            .client(OkHttpClient())
            .domain("http://www.baidu.com")
            .apply()
    }

}