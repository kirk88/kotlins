package com.hao.reader

import android.app.Application
import android.content.Context
import android.util.Log
import com.hao.reader.ui.DeviceIdUtils
import com.nice.kothttp.OkHttpConfiguration
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextHolder.init(this)

        OkHttpConfiguration.Setter()
            .client(
                OkHttpClient.Builder().addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                ).build()
            )
            .baseUrl("http://124.71.62.192:8090/")
            .apply()

        Log.e("TAGTAG" , DeviceIdUtils.getDeviceId(this).lowercase())
    }

}

private object ApplicationContextHolder {

    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

}

val applicationContext: Context
    get() = ApplicationContextHolder.applicationContext