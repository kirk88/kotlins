package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.helper.decodeBase64ToString
import com.easy.kotlins.helper.encodeBase64ToString
import com.easy.kotlins.helper.toast
import com.easy.kotlins.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : NiceActivity(R.layout.activity_main) {

    private val progressView: ProgressView by progressViews()


    override fun onBindView(savedInstanceState: Bundle?) {


        val str = "123"

        val en = str.encodeBase64ToString()

        println(en)

        println(en.decodeBase64ToString())


        val titleBar = findViewById<TitleBar>(R.id.titleBar)


        titleBar.setOnTitleClickListener{
            toast("title")
        }

        titleBar.setOnSubtitleClickListener{
            toast("subtitle")

        }

        val loader = findViewById<LoaderLayout>(R.id.loader_layout)
        Log.e("TAGTAG", "att: ${loader.isAttachedToWindow}")

        loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)


        lifecycleScope.launch {
           delay(1000)


            loader.showLoading()

            delay(1000)

            loader.showEmpty()


            delay(1000)

            loader.showError()


            delay(50)

            loader.showContent()
        }

    }

}