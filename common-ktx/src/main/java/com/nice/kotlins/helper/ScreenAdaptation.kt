package com.nice.kotlins.helper

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.res.Configuration

object ScreenAdaptation {

    private const val DESIGN_SCREEN_WIDTH: Float = 446.5.toFloat()

    private var noncompatDensity: Float = 0.toFloat()
    private var noncompatScaledDensity: Float = 0.toFloat()


    fun setCustomDensity(activity: Activity) {
        val application = activity.application
        val appDisplayMetrics = application.resources.displayMetrics

        if (noncompatDensity == 0.toFloat()
            && noncompatScaledDensity == 0.toFloat()
        ) {
            noncompatDensity = appDisplayMetrics.density
            noncompatScaledDensity = appDisplayMetrics.scaledDensity
            application.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    if (newConfig.fontScale > 0) {
                        noncompatScaledDensity = application.resources.displayMetrics.scaledDensity
                    }
                }

                override fun onLowMemory() {
                }
            })
        }

        val targetDensity: Float = appDisplayMetrics.widthPixels / DESIGN_SCREEN_WIDTH
        val targetScaledDensity: Float = targetDensity * (noncompatScaledDensity / noncompatDensity)
        val targetDensityDpi: Int = (targetDensity * 160).toInt()

        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi

        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
    }

}