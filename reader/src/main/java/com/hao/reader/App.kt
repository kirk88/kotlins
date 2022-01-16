package com.hao.reader

import android.app.Application
import android.content.Context
import com.nice.kothttp.OkHttpConfiguration

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextHolder.init(this)

        OkHttpConfiguration.Setter().apply()
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