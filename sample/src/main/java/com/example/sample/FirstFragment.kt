package com.example.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.nice.kotlins.app.NiceViewModelFragment
import com.nice.kotlins.app.ScreenCompatAdapter
import com.nice.kotlins.app.ScreenCompatStrategy
import com.nice.kotlins.event.Event
import com.nice.kotlins.event.event
import com.nice.kotlins.helper.add
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.showIme

class FirstFragment : NiceViewModelFragment<TestViewModel>(R.layout.fragment_first),
    ScreenCompatAdapter {

    override val screenCompatStrategy: ScreenCompatStrategy
        get() = ScreenCompatStrategy.NONE

    override val viewModel: TestViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editText = findViewById<EditText>(R.id.edittext)
        findViewById<Button>(R.id.button).doOnClick {
            editText.showIme()
        }

        viewModel.event = event("hello world")

        childFragmentManager.commit {
            add<ChildFragment>(R.id.frame_container)
        }
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