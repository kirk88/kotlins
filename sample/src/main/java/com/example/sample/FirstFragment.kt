package com.example.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.nice.kotlins.app.NiceViewModelFragment
import com.nice.kotlins.event.Event
import com.nice.kotlins.helper.doOnClick
import com.nice.kotlins.helper.showIme

class FirstFragment: NiceViewModelFragment<TestViewModel>(R.layout.fragment_first) {

    override val viewModel: TestViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editText = findViewById<EditText>(R.id.edittext)
        findViewById<Button>(R.id.button).doOnClick {
            editText.showIme()
        }

    }

    override fun dispatchViewModelEvent(event: Event): Boolean {
        Log.e("TAGTAG", "fragment dispatchViewModelEvent: "+event.message)
        return super.dispatchViewModelEvent(event)
    }

    override fun onInterceptViewModelEvent(event: Event): Boolean {
        Log.e("TAGTAG", "fragment onInterceptViewModelEvent: "+event.message)
        return super.onInterceptViewModelEvent(event)
    }

    override fun onViewModelEvent(event: Event): Boolean {
        Log.e("TAGTAG", "fragment onViewModelEvent: "+event.message)
        return super.onViewModelEvent(event)
    }

}