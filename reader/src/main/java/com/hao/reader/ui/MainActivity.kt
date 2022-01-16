package com.hao.reader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.hao.reader.extension.setDecorFitsSystemWindows
import com.nice.kothttp.OkHttpMethod
import com.nice.kothttp.OkWebSocketResponse
import com.nice.kothttp.buildHttpCall
import com.nice.kothttp.buildWebSocketCall
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import okhttp3.Response


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)


        val call = buildHttpCall<Response> {

            url("mzzkd/userApp/wx_login.do")

            queryParameters {
                "code" += "061fNyFa1jcztC0frdHa1MQupb0fNyFy"
            }
        }

        call.make().onEach {
            Log.e("TAGTAG", "result: $it")
        }.catch {
            Log.e("TAGTAG", it.message, it)
        }.launchIn(lifecycleScope)
    }

}