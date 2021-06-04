package com.nice.kotlins.app

import android.app.Activity
import android.content.res.Resources
import android.util.Log

internal object ScreenAdaptation {

    private val TAG = ScreenAdaptation::class.simpleName

    fun setCustomDensityIfNeed(activity: Activity) {
        val application = activity.application

        val activityAdapter = activity as? ScreenCompatAdapter
        val appAdapter = application as? ScreenCompatAdapter

        val screenCompatStrategy =
            activityAdapter?.screenCompatStrategy ?: appAdapter?.screenCompatStrategy
        val baseScreenWidth = activityAdapter?.baseScreenWidth ?: appAdapter?.baseScreenWidth
        val baseScreenHeight = activityAdapter?.baseScreenHeight ?: appAdapter?.baseScreenHeight

        if (screenCompatStrategy == null || baseScreenWidth == null || baseScreenHeight == null) {
            return
        }

        val systemDisplayMetrics = Resources.getSystem().displayMetrics

        val targetDensity: Float = when (screenCompatStrategy) {
            ScreenCompatStrategy.BASE_ON_WIDTH -> systemDisplayMetrics.widthPixels / baseScreenWidth.toFloat()
            ScreenCompatStrategy.BASE_ON_HEIGHT -> systemDisplayMetrics.heightPixels / baseScreenHeight.toFloat()
            else -> systemDisplayMetrics.density
        }
        val targetScaledDensity: Float = if (screenCompatStrategy == ScreenCompatStrategy.NONE) {
            systemDisplayMetrics.scaledDensity
        } else {
            targetDensity * (systemDisplayMetrics.scaledDensity / systemDisplayMetrics.density)
        }
        val targetDensityDpi: Int = (targetDensity * 160).toInt()

        setDensity(activity, targetDensity, targetScaledDensity, targetDensityDpi)
    }

    private fun setDensity(
        activity: Activity,
        targetDensity: Float,
        targetScaledDensity: Float,
        targetDensityDpi: Int,
    ) {
        val appDisplayMetrics = activity.application.resources.displayMetrics
        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi

        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi

        Log.i(TAG, "density: $targetDensity, scaledDensity: $targetScaledDensity, densityDpi: $targetDensityDpi")
    }

}

enum class ScreenCompatStrategy {
    NONE, BASE_ON_WIDTH, BASE_ON_HEIGHT
}

interface ScreenCompatAdapter {

    val screenCompatStrategy: ScreenCompatStrategy get() = ScreenCompatStrategy.BASE_ON_WIDTH

    val baseScreenWidth: Int get() = DEFAULT_BASE_SCREEN_WIDTH

    val baseScreenHeight: Int get() = DEFAULT_BASE_SCREEN_HEIGHT

    companion object {
        private const val DEFAULT_BASE_SCREEN_WIDTH = 360
        private const val DEFAULT_BASE_SCREEN_HEIGHT = 640
    }

}