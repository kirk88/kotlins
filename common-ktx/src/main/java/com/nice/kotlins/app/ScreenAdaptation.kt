package com.nice.kotlins.app

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import com.nice.kotlins.helper.statusBarHeight

internal object ScreenAdaptation {

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private fun applyCustomDensityIfNeed(target: Any, activity: Activity) {
        val application = activity.application

        val appAdapter = application as? ScreenCompatAdapter
        val targetAdapter = target as? ScreenCompatAdapter

        val screenCompatStrategy = targetAdapter?.screenCompatStrategy ?: appAdapter?.screenCompatStrategy
        val screenCompatWidth = targetAdapter?.screenCompatWidth ?: appAdapter?.screenCompatWidth
        val screenCompatHeight = targetAdapter?.screenCompatHeight ?: appAdapter?.screenCompatHeight
        val screenCompatUselessHeight = targetAdapter?.screenCompatUselessHeight ?: appAdapter?.screenCompatUselessHeight

        if (screenCompatStrategy == null || screenCompatWidth == null
                || screenCompatHeight == null || screenCompatUselessHeight == null
        ) {
            return
        }

        val systemDisplayMetrics = Resources.getSystem().displayMetrics

        val targetDensity: Float = when (screenCompatStrategy) {
            ScreenCompatStrategy.BASE_ON_WIDTH -> systemDisplayMetrics.widthPixels / screenCompatWidth.toFloat()
            ScreenCompatStrategy.BASE_ON_HEIGHT -> (systemDisplayMetrics.heightPixels - screenCompatUselessHeight) / screenCompatHeight.toFloat()
            ScreenCompatStrategy.AUTO -> {
                if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    systemDisplayMetrics.widthPixels / screenCompatWidth.toFloat()
                } else {
                    (systemDisplayMetrics.heightPixels - screenCompatUselessHeight) / screenCompatHeight.toFloat()
                }
            }
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
            targetDensityDpi: Int
    ) {
        val appDisplayMetrics = activity.application.resources.displayMetrics
        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi

        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
    }

    private class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            applyCustomDensityIfNeed(activity, activity)
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

}

enum class ScreenCompatStrategy {
    NONE, AUTO, BASE_ON_WIDTH, BASE_ON_HEIGHT
}

interface ScreenCompatAdapter {

    val screenCompatStrategy: ScreenCompatStrategy get() = ScreenCompatStrategy.BASE_ON_WIDTH
    val screenCompatWidth: Int get() = DEFAULT_SCREEN_COMPAT_WIDTH
    val screenCompatHeight: Int get() = DEFAULT_SCREEN_COMPAT_HEIGHT
    val screenCompatUselessHeight: Int get() = DEFAULT_SCREEN_COMPAT_USELESS_HEIGHT

    companion object {
        private const val DEFAULT_SCREEN_COMPAT_WIDTH = 360
        private const val DEFAULT_SCREEN_COMPAT_HEIGHT = 640

        private val DEFAULT_SCREEN_COMPAT_USELESS_HEIGHT = applicationContext.statusBarHeight
    }

}