package com.nice.common.helper

import android.content.Context
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.nice.common.R

class GestureDelegate internal constructor(context: Context) : GestureDetector.SimpleOnGestureListener() {

    private val detector = GestureDetector(context, this)

    private val listeners = mutableListOf<GestureDetector.OnGestureListener>()

    internal fun onTouchEvent(e: MotionEvent): Boolean = detector.onTouchEvent(e)

    @PublishedApi
    internal fun addListener(listener: GestureDetector.OnGestureListener) {
        listeners.add(listener)
    }

    override fun onDown(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener.onDown(e)) return true
        }
        return super.onDown(e)
    }

    override fun onShowPress(e: MotionEvent) {
        for (listener in listeners) {
            listener.onShowPress(e)
        }
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener.onSingleTapUp(e)) return true
        }
        return super.onSingleTapUp(e)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        for (listener in listeners) {
            if (listener.onScroll(e1, e2, distanceX, distanceY)) return true
        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onLongPress(e: MotionEvent) {
        for (listener in listeners) {
            listener.onLongPress(e)
        }
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        for (listener in listeners) {
            if (listener.onFling(e1, e2, velocityX, velocityY)) return true
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener && listener.onSingleTapConfirmed(e)) return true
        }
        return super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener && listener.onDoubleTap(e)) return true
        }
        return super.onDoubleTap(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener && listener.onDoubleTapEvent(e)) return true
        }
        return super.onDoubleTapEvent(e)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onContextClick(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnContextClickListener && listener.onContextClick(e)) return true
        }
        return super.onContextClick(e)
    }

}

@Suppress("ClickableViewAccessibility")
fun GestureDelegate(view: View): GestureDelegate {
    val delegate = view.getTag(R.id.gesture_delegate_id) as? GestureDelegate ?: GestureDelegate(view.context).also {
        view.setOnTouchListener { _, event -> it.onTouchEvent(event) }
    }
    return delegate
}

inline fun GestureDelegate.doOnDown(crossinline action: (MotionEvent) -> Boolean) {
    addListener(object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return action.invoke(e)
        }
    })
}

inline fun GestureDelegate.doOnShowPress(crossinline action: (MotionEvent) -> Unit) {
    addListener(object : GestureDetector.SimpleOnGestureListener() {
        override fun onShowPress(e: MotionEvent) {
            action.invoke(e)
        }
    })
}