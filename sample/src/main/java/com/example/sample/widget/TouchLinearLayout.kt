package com.example.sample.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.LinearLayoutCompat

class TouchLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    private var intercept = true
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("TAGTAG", "onInterceptTouchEvent: $ev")
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e("TAGTAG", "onTouch: $event")
        return super.onTouchEvent(event)
    }

}