package com.example.sample

import android.app.Application
import android.content.Context

private object ApplicationContextHolder {

    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

}

val applicationContext: Context
    get() = ApplicationContextHolder.applicationContext

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextHolder.init(this)
    }

}