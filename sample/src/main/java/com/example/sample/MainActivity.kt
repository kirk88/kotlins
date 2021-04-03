package com.example.sample

import android.os.Bundle
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.helper.decodeBase64ToString
import com.easy.kotlins.helper.encodeBase64ToString
import com.easy.kotlins.helper.toast
import com.easy.kotlins.widget.ProgressView
import com.easy.kotlins.widget.TitleBar
import com.easy.kotlins.widget.progressViews

class MainActivity : NiceActivity(R.layout.activity_main) {

    private val progressView: ProgressView by progressViews()


    override fun onBindView(savedInstanceState: Bundle?) {
        progressView.showProgress()


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
    }

}