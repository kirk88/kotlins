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

    internal fun onTouchEvent(e: MotionEvent): Boolean = detector.onTouchEvent(e)

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

    @TargetApi(Build.VERSION_CODES.M)
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
        view.setTag(R.id.gesture_delegate_id, it)
        view.setOnTouchListener { _, event -> it.onTouchEvent(event) }
    }
    return delegate
}

inline fun GestureDelegate.addListener(
    crossinline onDown: (e: MotionEvent) -> Boolean = { _ -> false },
    crossinline onShowPress: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onSingleTapUp: (e: MotionEvent) -> Boolean = { _ -> false },
    crossinline onScroll: (e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Boolean = { _, _, _, _ -> false },
    crossinline onLongPress: (e: MotionEvent) -> Unit = { _ -> },
    crossinline onFling: (e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Boolean = { _, _, _, _ -> false },
    crossinline onSingleTapConfirmed: (e: MotionEvent) -> Boolean = { _ -> false },
    crossinline onDoubleTap: (e: MotionEvent) -> Boolean = { _ -> false },
    crossinline onDoubleTapEvent: (e: MotionEvent) -> Boolean = { _ -> false },
    crossinline onContextClick: (e: MotionEvent) -> Boolean = { _ -> false }
): GestureDetector.OnGestureListener {
    val listener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = onDown.invoke(e)
        override fun onShowPress(e: MotionEvent) = onShowPress.invoke(e)
        override fun onSingleTapUp(e: MotionEvent): Boolean = onSingleTapUp.invoke(e)
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean =
            onScroll.invoke(e1, e2, distanceX, distanceY)

        override fun onLongPress(e: MotionEvent) = onLongPress.invoke(e)
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean =
            onFling.invoke(e1, e2, velocityX, velocityY)

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean = onSingleTapConfirmed.invoke(e)
        override fun onDoubleTap(e: MotionEvent): Boolean = onDoubleTap.invoke(e)
        override fun onDoubleTapEvent(e: MotionEvent): Boolean = onDoubleTapEvent.invoke(e)
        override fun onContextClick(e: MotionEvent): Boolean = onContextClick.invoke(e)
    }
    addListener(listener)
    return listener
}

inline fun GestureDelegate.doOnDown(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onDown = action)

inline fun GestureDelegate.doOnShowPress(crossinline action: (MotionEvent) -> Unit) =
    addListener(onShowPress = action)

inline fun GestureDelegate.doOnSingleTapUp(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onSingleTapUp = action)

inline fun GestureDelegate.doOnScroll(crossinline action: (e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Boolean) =
    addListener(onScroll = action)

inline fun GestureDelegate.doOnLongPress(crossinline action: (MotionEvent) -> Unit) =
    addListener(onLongPress = action)

inline fun GestureDelegate.doOnFling(crossinline action: (e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Boolean) =
    addListener(onFling = action)

inline fun GestureDelegate.doOnSingleTapConfirmed(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onSingleTapConfirmed = action)

inline fun GestureDelegate.doOnDoubleTap(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onDoubleTap = action)

inline fun GestureDelegate.doOnDoubleTapEvent(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onDoubleTapEvent = action)

@RequiresApi(Build.VERSION_CODES.M)
inline fun GestureDelegate.doOnContextClick(crossinline action: (MotionEvent) -> Boolean) =
    addListener(onContextClick = action)