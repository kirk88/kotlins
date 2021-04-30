package com.example.sample

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.core.text.HtmlCompat
import androidx.viewbinding.ViewBinding
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.attachTo(this)

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
            useWideViewPort=true
        }

        webView.webViewClient = object : WebViewClient(){

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

    }

}