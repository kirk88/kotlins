package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import com.example.sample.databinding.ActivityMainBinding
import com.faendir.rhino_android.RhinoAndroidHelper
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.plusAssign
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews
import org.mozilla.javascript.ImporterTopLevel


class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    private val tipView: TipView by tipViews()

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
                "key" to "value"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
        }

        val liveEvent = MutableLiveEvent<String>()
        liveEvent.observe(this) {
            Log.e("TAGTAG", "event: $it")
        }

        liveEvent += "event1"

        liveEvent += "event2"

        val helper = RhinoAndroidHelper(this)
        val ctx = helper.enterContext()
        ctx.optimizationLevel = 1
        ctx.applicationClassLoader = application.classLoader
        val scope = ImporterTopLevel(ctx)
        scope.put("test", scope, Test().toJs(scope))
        val result = ctx.evaluateString<List<String>>(scope, "test.add('123')", "tt")
        Log.e("TAGTAG", "result: ${result}")
    }


    class Test {

        fun add(string: String): List<String> {
            return listOf("$string ppp")
        }

    }

}

