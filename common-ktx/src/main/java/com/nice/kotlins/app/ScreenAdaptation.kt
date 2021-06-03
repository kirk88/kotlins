package com.nice.kotlins.app

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.pm.PackageManager
import android.content.res.Configuration

object ScreenAdaptation {

    private var designScreenWidth: Float = 0.toFloat()
    private var designScreenHeight: Float = 0.toFloat()

    private var defaultDensity: Float = 0.toFloat()
    private var defaultScaledDensity: Float = 0.toFloat()

    fun setCustomDensityIfNeed(activity: Activity) {
        val application = activity.application

        if (designScreenWidth == 0.toFloat()) {
            val appInfo = application.packageManager.getApplicationInfo(
                application.packageName,
                PackageManager.GET_META_DATA
            )
            val value = appInfo.metaData.get("DESIGN_SCREEN_WIDTH").toString().toFloatOrNull()
                ?: return
            designScreenWidth = value
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

        val targetDensity: Float = appDisplayMetrics.widthPixels / designScreenWidth
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

    private fun setDensity(activity: Activity, density: Float, scaleDensity: Float, densityDpi: Float){

    }

}