package com.example.sample

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.helper.installTo
import com.nice.kotlins.helper.onClick
import com.nice.kotlins.helper.startActivity
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.installTo(this)

        title = "我是谁"

        val loader = binding.loaderLayout
        val titleBar = binding.titleBar
        val fab = binding.fab
        val checker = binding.checker

        fab.onClick {
            startActivity<SecondActivity>()
        }

//        lifecycleScope.launch(Dispatchers.Default) {
//            progressView.showProgress("你好啊")
//
//            delay(1000)
//
//            progressView.dismissProgress()
//
//
//            delay(200)
//
//            progressView.showProgress("还好吧")
//
//
//            delay(2000)
//            progressView.dismissProgress()
//        }

        lifecycleScope.launch {

            checker.isChecked = true

            loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)

            delay(1000)

            loader.showContent()

            delay(1000)

            loader.showEmpty()

            delay(1000)

            loader.showError()
        }

    }

}