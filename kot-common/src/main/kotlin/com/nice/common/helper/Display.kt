package com.nice.common.helper

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.nice.common.R
import kotlin.math.pow
import kotlin.math.sqrt

fun Context.getDisplaySize(size: Rect) {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        size.set(wm.currentWindowMetrics.bounds)
    } else {
        val point = Point().also { wm.defaultDisplay.getSize(it) }
        size.set(0, 0, point.x, point.y)
    }
}

fun Context.getDisplayRealSize(size: Rect) {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        size.set(wm.currentWindowMetrics.bounds)
    } else {
        val point = Point().also { wm.defaultDisplay.getRealSize(it) }
        size.set(0, 0, point.x, point.y)
    }
}

fun Context.getDisplayMetrics(metrics: DisplayMetrics) {
    resources.displayMetrics.setTo(metrics)
}

val Context.displayWidth: Int
    get() = Rect().also { getDisplaySize(it) }.width()

val Context.displayHeight: Int
    get() = Rect().also { getDisplaySize(it) }.height()

val Context.displayRealWidth: Int
    get() = Rect().also { getDisplayRealSize(it) }.width()

val Context.displayRealHeight: Int
    get() = Rect().also { getDisplayRealSize(it) }.height()

val Context.statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

val Context.navigationBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

val Context.defaultActionBarHeight: Int
    get() {
        val ta = obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
        val result = ta.getDimensionPixelSize(0, 0)
        ta.recycle()
        return result
    }

val Activity.actionBarHeight: Int
    get() = actionBar?.height.orElse { defaultActionBarHeight }

val AppCompatActivity.actionBarHeight: Int
    get() = supportActionBar?.height.orElse { defaultActionBarHeight }

val Context.isTabletDevice: Boolean
    get() {
        val dm = resources.displayMetrics
        val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
        val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
        val screenInches = sqrt(x + y)
        return screenInches >= 7.0
    }