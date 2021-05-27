package com.example.sample

import androidx.multidex.MultiDexApplication
import com.faendir.rhino_android.RhinoAndroidHelper
import com.google.gson.GsonBuilder
import com.nice.kotlins.helper.GsonProvider
import com.nice.kotlins.http.OkFaker
import okhttp3.OkHttpClient

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
    }

}