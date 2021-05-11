package com.example.sample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.*
import com.nice.kotlins.http.DefaultOkDownloadMapper
import com.nice.kotlins.http.OkFaker
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        val titleBar = binding.titleBar
        val fab = binding.fab
        val webView = binding.webView


        fab.onClick {
//            startActivity<SecondActivity>()
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "ppppppp"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }

        }

        val deviceId = DeviceIdUtil.getDeviceId(this)


        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("TAGTAG", "start url: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.e("TAGTAG", "finish url: $url")
            }
        }


        webView.loadUrl("file:///android_asset/html/index.html?file:///android_asset/html/0110219001619502821.pdf")

        Log.e("TAGTAG", "deviceId: $deviceId")


        var faker: OkFaker<File>? = null
        lifecycleScope.launch {
            val f = suspendBlocking(ExecutorDispatchers.IO) {
                var start = System.currentTimeMillis()
                faker = OkFaker.get<File> {
                    client(OkHttpClient())

                    url("https://r4---sn-ni57rn7k.gvt1.com/edgedl/android/studio/ide-zips/4.2.0.17/android-studio-ide-202.6987402-windows.zip?cms_redirect=yes&mh=hz&mip=59.53.31.32&mm=28&mn=sn-ni57rn7k&ms=nvh&mt=1607303531&mv=m&mvi=4&pl=20&shardbypass=yes")

                    val f =
                        File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "download.exe")
                    mapResponse(DefaultOkDownloadMapper(f.absolutePath, true) { readBytes, totalBytes ->
                        if(System.currentTimeMillis() - start > 2000){
                            Log.e("TAGTAG", "$readBytes   $totalBytes")
                            start = System.currentTimeMillis()
                        }
                    })
                }.build()

                faker?.executeOrNull()
            }

            Log.e("TAGTAG", f?.absolutePath.ifNull { "null" })
        }

    }

}