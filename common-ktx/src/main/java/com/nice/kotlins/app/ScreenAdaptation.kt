package com.nice.kotlins.app

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.pm.PackageManager
import android.content.res.Configuration

object ScreenAdaptation {

    private var compatScreenWidth: Float = 0.toFloat()

    private var defaultDensity: Float = 0.toFloat()
    private var defaultScaledDensity: Float = 0.toFloat()

    fun setCustomDensityIfNeed(activity: Activity) {
        val application = activity.application

        if (compatScreenWidth == 0.toFloat()) {
            val appInfo = application.packageManager.getApplicationInfo(
                application.packageName,
                PackageManager.GET_META_DATA
            )
            val value = appInfo.metaData.get("COMPAT_SCREEN_WIDTH").toString().toFloatOrNull()
                ?: return
            compatScreenWidth = value
        }

        val appDisplayMetrics = application.resources.displayMetrics

        if (defaultDensity == 0.toFloat()
            && defaultScaledDensity == 0.toFloat()
        ) {
            defaultDensity = appDisplayMetrics.density
            defaultScaledDensity = appDisplayMetrics.scaledDensity
            application.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    if (newConfig.fontScale > 0) {
                        defaultScaledDensity = application.resources.displayMetrics.scaledDensity
                    }
                }

                override fun onLowMemory() {
                }
            })
        }

        val targetDensity: Float =
            appDisplayMetrics.widthPixels / compatScreenWidth
        val targetScaledDensity: Float = targetDensity * (defaultScaledDensity / defaultDensity)
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