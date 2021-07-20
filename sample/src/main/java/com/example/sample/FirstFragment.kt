package com.example.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.nice.common.app.NiceViewModelFragment
import com.nice.common.event.Event
import com.nice.common.event.event
import com.nice.common.helper.add
import com.nice.common.helper.doOnClick
import com.nice.common.helper.showIme
import com.nice.common.helper.showSnackBar

class FirstFragment : NiceViewModelFragment<TestViewModel>(R.layout.fragment_first) {

    override val viewModel: TestViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editText = findViewById<EditText>(R.id.edittext)
        findViewById<Button>(R.id.button).doOnClick {
            editText.showIme()

            editText.postDelayed({ showSnackBar("你好", Snackbar.LENGTH_INDEFINITE) }, 2000)
        }

        viewModel.event = event("hello world")

        childFragmentManager.add<ChildFragment>(R.id.frame_container)
    }

    override fun dispatchViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "dispatchViewModelEvent: " + event.message)
        return super.dispatchViewModelEvent(event)
    }

    override fun onInterceptViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "onInterceptViewModelEvent: " + event.message)
        return super.onInterceptViewModelEvent(event)
    }

    override fun onViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "onViewModelEvent: " + event.message)
        return super.onViewModelEvent(event)
    }

    companion object {
        private val TAG = FirstFragment::class.simpleName
    }

}