package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.attachTo
import com.nice.kotlins.helper.onClick
import com.nice.kotlins.helper.string
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.LoaderView
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews

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
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "ppppppp"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
        }

        val deviceId = DeviceIdUtil.getDeviceId(this)

        binding.textView.string = deviceId

        Log.e("TAGTAG", "deviceId: $deviceId")


        //075E7351B1765B249CC9010525484FD8A203E042
        //075E7351B1765B249CC9010525484FD8A203E042

        //619CBA39E85BBC6FE18C762BEFD1105ADA4F742C
    }

}