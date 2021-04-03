package com.example.sample

import android.os.Bundle
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.widget.ProgressView
import com.easy.kotlins.widget.progressViews

class MainActivity : NiceActivity(R.layout.activity_main) {

    private val progressView: ProgressView by progressViews()


    override fun onBindView(savedInstanceState: Bundle?) {
        progressView.showProgress()
    }

}