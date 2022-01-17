package com.hao.reader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.hao.reader.extension.setDecorFitsSystemWindows
import com.nice.kothttp.OkRequestMethod
import com.nice.kothttp.buildHttpCall
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.Response


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(false)

        val call = buildHttpCall<Response>(OkRequestMethod.Post) {

            url { "mzzkd/userApp/wx_login.do" }

            formParameters {
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