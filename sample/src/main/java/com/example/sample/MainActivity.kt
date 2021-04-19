package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.attachTo
import com.nice.kotlins.helper.onClick
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.attachTo(this)

        title = "Home"

        val loader = binding.loaderLayout
        val titleBar = binding.titleBar
        val fab = binding.fab


        loader.setDefaultView(LoaderView.TYPE_CONTENT_VIEW)

        fab.onClick {
//            startActivity<SecondActivity>()
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(this, "key" to "ppppppp") {
                Log.e("TAGTAG", "" +it.component1() + " " + it.component2())
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            progressView.showProgress("你好啊")

            delay(1000)

            progressView.dismissProgress()


            delay(200)

            progressView.showProgress("还好吧")


            delay(2000)
            progressView.dismissProgress()
        }

    }

}