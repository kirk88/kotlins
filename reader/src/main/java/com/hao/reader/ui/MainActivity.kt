package com.hao.reader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.hao.reader.extension.setDecorFitsSystemWindows
import com.nice.kothttp.OkWebSocketResponse
import com.nice.kothttp.httpCallFlow
import com.nice.kothttp.webSocketCallFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import java.io.IOException


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)
        setContent { ReaderApp() }


        val callFlow = httpCallFlow<String> {

            client(OkHttpClient())

            url("https://www.baidu.com")

            headers {
                add("ContentType", "application/json")
            }

            queryParameters {
                add("kw", "百度")
            }

            addRequestInterceptor {
                it.newBuilder().build()
            }

            addResponseInterceptor {
                it.newBuilder().build()
            }

        }

        callFlow.onEach {
            Log.e("TAG", "result: $it")
        }.launchIn(lifecycleScope)

        webSocketCallFlow {
            url("ws://xxx.xxx.xxx")
        }.retryWhen { cause, attempt ->
            cause is IOException
        }.onEach {
            when (it) {
                is OkWebSocketResponse.StringMessage -> {
                    Log.e("TAG", "message: ${it.text}")
                }
                is OkWebSocketResponse.Failure -> {
                    Log.e("TAG", "error: ${it.error}")
                }
                is OkWebSocketResponse.ByteStringMessage -> TODO()
                is OkWebSocketResponse.Closed -> TODO()
                is OkWebSocketResponse.Closing -> TODO()
                is OkWebSocketResponse.Open -> TODO()
            }
        }.launchIn(lifecycleScope)
    }

}
