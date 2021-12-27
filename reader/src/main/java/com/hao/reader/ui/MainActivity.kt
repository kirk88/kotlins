package com.hao.reader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.hao.reader.extension.setDecorFitsSystemWindows
import com.nice.kothttp.OkWebSocketResponse
import com.nice.kothttp.buildHttpCall
import com.nice.kothttp.buildWebSocketCall
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)
        setContent { ReaderApp() }


        val call = buildHttpCall<String> {

            url("https://www.baidu.com")

            headers {
                "ContentType" += "application/json"
            }

            queryParameters {
                "kw" += "百度"
            }

            interceptRequest {
                it.newBuilder().build()
            }

            interceptResponse {
                it.newBuilder().build()
            }

        }

        call.make().onEach {
            Log.e("TAG", "result: $it")
        }.launchIn(lifecycleScope)


        val socket = buildWebSocketCall {
            client(OkHttpClient())

            url("ws://xxx.xxx.xxx")
        }.make()

        socket.response.catch {

        }.onEach {
            when (it) {
                is OkWebSocketResponse.StringMessage -> {
                    Log.e("TAG", "message: ${it.text}")
                }
                is OkWebSocketResponse.Failure -> {
                    Log.e("TAG", "error: ${it.error}")
                }
                is OkWebSocketResponse.ByteStringMessage -> TODO("onMessage")
                is OkWebSocketResponse.Closed -> TODO("onClosed")
                is OkWebSocketResponse.Closing -> TODO("onClosing")
                is OkWebSocketResponse.Open -> TODO("onOpen")
            }
        }.launchIn(lifecycleScope)
    }

}
