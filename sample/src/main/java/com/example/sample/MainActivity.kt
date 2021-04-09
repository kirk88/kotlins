package com.example.sample

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.easy.kotlins.app.NiceActivity
import com.easy.kotlins.helper.installTo
import com.easy.kotlins.helper.startActivity
import com.easy.kotlins.helper.viewBindings
import com.easy.kotlins.widget.*
import com.example.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
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

        loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)

        titleBar.setOnTitleClickListener {
            startActivity<SecondActivity>()
        }

//        lifecycleScope.launch {
//            progressView.showProgress()
//
//            delay(1000)
//
//            progressView.dismissProgress()
//
//
//            delay(200)
//
//            progressView.showProgress()
//
//
//            delay(2000)
//            progressView.dismissProgress()
//        }

        lifecycleScope.launch {
            delay(1000)


            loader.showLoading()

            delay(1000)

            loader.showEmpty()

        }

        lifecycleScope.launch(Dispatchers.IO) {
            loader.showError()


            delay(3000)

            loader.showContent()
        }
    }

}