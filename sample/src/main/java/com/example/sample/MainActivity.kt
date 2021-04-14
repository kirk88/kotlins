package com.example.sample

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.navigationIconTint
import com.nice.kotlins.widget.progressViews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.installTo(this)

        val loader = binding.loaderLayout
        val titleBar = binding.titleBar
        val fab = binding.fab


        titleBar.setOnTitleClickListener{
            toast("click title")
        }

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

            loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)

            delay(1000)

            findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.apply {
                setExpandedTitleColor(Color.WHITE)

                setCollapsedTitleTextColor(Color.WHITE)

                Log.e("TAGTAG", "title: $title")
            }


            loader.showContent()

            delay(1000)

            loader.showEmpty()

            delay(1000)

            loader.showError()

            delay(1000)

            loader.showContent()
        }

    }

}