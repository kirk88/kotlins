package com.example.sample

import android.os.Bundle
import android.util.Log
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.helper.toast
import com.easy.kotlins.widget.*

class MainActivity : NiceActivity(R.layout.activity_main) {

    private val progressView: ProgressView by progressViews()


    override fun onBindView(savedInstanceState: Bundle?) {
        val titleBar = findViewById<TitleBar>(R.id.titleBar)


        titleBar?.setOnTitleClickListener {
            toast("title")
        }

        titleBar?.setOnSubtitleClickListener {
            toast("subtitle")

        }


        val loader = findViewById<LoaderLayout>(R.id.loader_layout)

        loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)

//        lifecycleScope.launch {
//           delay(1000)
//
//
//            loader.showLoading()
//
//            delay(1000)
//
//            loader.showEmpty()
//
//
//            delay(1000)
//
//            loader.showError()
//
//
//            delay(50)
//
//            loader.showContent()
//        }

    }

}