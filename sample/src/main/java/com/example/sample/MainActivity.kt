package com.example.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import com.example.sample.databinding.ActivityMainBinding
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.setContentView
import com.nice.kotlins.helper.viewBindings
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.progressViews


class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        val titleBar = binding.titleBar
        val fab = binding.fab
        val pdfView=  binding.pdfView

//        pdfView.open(File("/storage/emulated/0/DoctorAideImg/doctor/.西医书籍/诊断/从症状到诊断——循证学指导（第2版）.pdf"))

//        pdfView.fromFile(File("/storage/emulated/0/DoctorAideImg/doctor/.西医书籍/诊断/从症状到诊断——循证学指导（第2版）.pdf"))
//            .defaultPage(0)
//            .fitEachPage(true)
//            .swipeHorizontal(true)
//            .swipeSimulation(true)
//            .pageFitPolicy(FitPolicy.BOTH)
//            .load()


        fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                this,
                "key" to "ppppppp"
            ) {
                Log.e("TAGTAG", "" + it.component1() + " " + it.component2())
            }
        }

    }


}