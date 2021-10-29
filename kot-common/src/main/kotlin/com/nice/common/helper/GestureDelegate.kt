@file:Suppress("UNUSED")

package com.nice.common.helper

import android.annotation.TargetApi
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

    private var consumeTouchEvent: Boolean = false

    internal fun onTouchEvent(e: MotionEvent): Boolean {
        detector.onTouchEvent(e)
        return consumeTouchEvent
    }

    internal fun setConsumeTouchEvent(consume: Boolean) {
        consumeTouchEvent = consume
    }

    fun addListener(listener: GestureDetector.OnGestureListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: GestureDetector.OnGestureListener) {
        listeners.remove(listener)
    }

    override fun onDown(e: MotionEvent): Boolean {
        for (listener in listeners) {
            listener.onDown(e)
        }
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        for (listener in listeners) {
            listener.onShowPress(e)
        }
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        for (listener in listeners) {
            listener.onSingleTapUp(e)
        }
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        for (listener in listeners) {
            listener.onScroll(e1, e2, distanceX, distanceY)
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        for (listener in listeners) {
            listener.onLongPress(e)
        }
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        for (listener in listeners) {
            listener.onFling(e1, e2, velocityX, velocityY)
        }
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener) listener.onSingleTapConfirmed(e)
        }
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener) listener.onDoubleTap(e)
        }
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnDoubleTapListener) listener.onDoubleTapEvent(e)
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onContextClick(e: MotionEvent): Boolean {
        for (listener in listeners) {
            if (listener is GestureDetector.OnContextClickListener) listener.onContextClick(e)
        }
        return false
    }

}

@Suppress("ClickableViewAccessibility")
fun GestureDelegate(view: View, consume: Boolean = false): GestureDelegate {
    val delegate = view.getTag(R.id.gesture_delegate_id) as? GestureDelegate ?: GestureDelegate(view.context).also {
        view.setTag(R.id.gesture_delegate_id, it)
        view.setOnTouchListener { _, event -> it.onTouchEvent(event); consume }
    }
    delegate.setConsumeTouchEvent(consume)
    return delegate
}

inline fun GestureDelegate.addListener(
    crossinline onDown: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onShowPress: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onSingleTapUp: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onScroll: (e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Unit = { _, _, _, _ -> },
    crossinline onLongPress: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onFling: (e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit = { _, _, _, _ -> },
    crossinline onSingleTapConfirmed: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onDoubleTap: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onDoubleTapEvent: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onContextClick: (e: MotionEvent) -> Unit = { _ -> }
): GestureDetector.OnGestureListener {
    val listener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            onDown.invoke(e)
            return false
        }

        override fun onShowPress(e: MotionEvent) = onShowPress.invoke(e)
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTapUp.invoke(e)
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            onScroll.invoke(e1, e2, distanceX, distanceY)
            return false
        }


        override fun onLongPress(e: MotionEvent) = onLongPress.invoke(e)
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            onFling.invoke(e1, e2, velocityX, velocityY)
            return false
        }


        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onSingleTapConfirmed.invoke(e)
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap.invoke(e)
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            onDoubleTapEvent.invoke(e)
            return false
        }

        override fun onContextClick(e: MotionEvent): Boolean {
            onContextClick.invoke(e)
            return false
        }
    }
    addListener(listener)
    return listener
}

inline fun GestureDelegate.doOnDown(crossinline action: (MotionEvent) -> Unit) =
    addListener(onDown = action)

inline fun GestureDelegate.doOnShowPress(crossinline action: (MotionEvent) -> Unit) =
    addListener(onShowPress = action)

inline fun GestureDelegate.doOnSingleTapUp(crossinline action: (MotionEvent) -> Unit) =
    addListener(onSingleTapUp = action)

inline fun GestureDelegate.doOnScroll(crossinline action: (e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Unit) =
    addListener(onScroll = action)

inline fun GestureDelegate.doOnLongPress(crossinline action: (MotionEvent) -> Unit) =
    addListener(onLongPress = action)

inline fun GestureDelegate.doOnFling(crossinline action: (e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit) =
    addListener(onFling = action)

inline fun GestureDelegate.doOnSingleTapConfirmed(crossinline action: (MotionEvent) -> Unit) =
    addListener(onSingleTapConfirmed = action)

inline fun GestureDelegate.doOnDoubleTap(crossinline action: (MotionEvent) -> Unit) =
    addListener(onDoubleTap = action)

inline fun GestureDelegate.doOnDoubleTapEvent(crossinline action: (MotionEvent) -> Unit) =
    addListener(onDoubleTapEvent = action)

@RequiresApi(Build.VERSION_CODES.M)
inline fun GestureDelegate.doOnContextClick(crossinline action: (MotionEvent) -> Unit) =
    addListener(onContextClick = action)