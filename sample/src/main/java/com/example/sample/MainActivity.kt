package com.example.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.lifecycle.liveData
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.*
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews


class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    private val tipView:TipView by tipViews()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        val titleBar = binding.titleBar
        val fab = binding.fab
        val pdfView = binding.pdfView

        fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "ppppppp"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
        }

        val liveEvent = MutableLiveEvent<String>()

        liveEvent.observe(this){
            Log.e("TAGTAG", "event: $it")
        }
        liveEvent += "event1"

        liveEvent += "event2"

        tipView.show("你好啊")
    }


}