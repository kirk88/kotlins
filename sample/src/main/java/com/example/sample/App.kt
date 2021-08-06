package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.common.app.ScreenCompatAdapter
import com.nice.common.app.ScreenCompatStrategy
import com.nice.common.helper.isTabletDevice
import com.nice.okfaker.DefaultOkDownloadMapper
import com.nice.okfaker.OkConfig
import com.nice.okfaker.OkFaker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = if (isTabletDevice) ScreenCompatStrategy.BASE_ON_HEIGHT
        else ScreenCompatStrategy.BASE_ON_WIDTH

    override fun onCreate() {
        super.onCreate()

    }

}

fun main() {
    OkFaker.setGlobalConfig(
        OkConfig.Builder()
            .baseUrl("http://www.baidu.com")
            .client(OkHttpClient())
            .build()
    )

    println(OkFaker.get<File>{

        client {
            //注意 HttpLoggingInterceptor.Level < BODY
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build()
        }

        url("/s")

        mapResponse(DefaultOkDownloadMapper("path", true){ readBytes, totalBytes ->
            //Main Thread
        })

    }.enqueue())
}