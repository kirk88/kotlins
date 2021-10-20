package com.example.sample

import androidx.multidex.MultiDexApplication
import com.googlecode.tesseract.android.TessBaseAPI
import com.nice.common.app.ScreenCompatAdapter
import com.nice.common.app.ScreenCompatStrategy
import com.nice.common.helper.isTabletDevice
import com.nice.kothttp.OkHttpConfig
import okhttp3.OkHttpClient
import java.io.File


class App : MultiDexApplication(), ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = if (isTabletDevice) ScreenCompatStrategy.BASE_ON_HEIGHT
        else ScreenCompatStrategy.BASE_ON_WIDTH

    override fun onCreate() {
        super.onCreate()

        OkHttpConfig.Setter()
            .client(OkHttpClient())
            .domain("http://www.baidu.com")
            .apply()
    }

}

fun main() {
    val tessApi = TessBaseAPI()
    tessApi.init("E:\\tessdata", "chi_sim")
    tessApi.setImage(File("E:\\Screenshot_20211015_131559.png"))
    println(tessApi.utF8Text)
}