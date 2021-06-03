package com.example.sample.spider

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.webkit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Closeable

class SpiderClient(context: Context) : Closeable {

    @SuppressLint("SetJavaScriptEnabled")
    private val delegate = WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            addJavascriptInterface(HtmlGetter(), "htmlGetter")
        }
        webViewClient = SpiderWebViewClient()
    }

//
//    fun open(url: String): WebPage {
//
//    }

    override fun close() {
        delegate.clearHistory()
        delegate.removeAllViews()
        delegate.destroy()
    }


    class HtmlGetter {

        @JavascriptInterface
        fun receiveSource(html: String) {

        }

    }

    private class SpiderWebViewClient : WebViewClient(), CoroutineScope by MainScope() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest,
        ): Boolean {
            return true
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest,
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError,
        ) {
            handler.proceed()
        }

        override fun onPageFinished(view: WebView, url: String) {
            launch {
                delay(3000)

                view.loadUrl(JS_OUTER_HTML)
            }
        }

        companion object {
            private const val JS_OUTER_HTML =
                "javascript:window.htmlGetter.receiveSource('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');"
        }

    }

}

class WebPage(source: String) {

}