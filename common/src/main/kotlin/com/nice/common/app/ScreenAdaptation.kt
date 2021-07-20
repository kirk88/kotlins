package com.nice.common.app

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.nice.common.applicationContext
import com.nice.common.helper.statusBarHeight

internal object ScreenAdaptation {

    @JvmStatic
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private fun applyCustomDensityIfNeed(activity: Activity, target: Any) {
        val application = activity.application

        val appAdapter = application as? ScreenCompatAdapter
        val targetAdapter = target as? ScreenCompatAdapter

        val screenCompatStrategy =
            targetAdapter?.screenCompatStrategy ?: appAdapter?.screenCompatStrategy
        val screenCompatWidth = targetAdapter?.screenCompatWidth ?: appAdapter?.screenCompatWidth
        val screenCompatHeight =
            targetAdapter?.screenCompatHeight ?: appAdapter?.screenCompatHeight
        val screenCompatUselessHeight =
            targetAdapter?.screenCompatUselessHeight ?: appAdapter?.screenCompatUselessHeight

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

        setDensity(activity.application.resources, targetDensity, targetScaledDensity, targetDensityDpi)
        setDensity(activity.resources, targetDensity, targetScaledDensity, targetDensityDpi)
    }

    private fun setDensity(
        resources: Resources,
        targetDensity: Float,
        targetScaledDensity: Float,
        targetDensityDpi: Int
    ) {
        val displayMetrics = resources.displayMetrics
        displayMetrics.density = targetDensity
        displayMetrics.scaledDensity = targetScaledDensity
        displayMetrics.densityDpi = targetDensityDpi
    }

    private class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(FragmentLifecycleCallbacks(), true)
            }

            applyCustomDensityIfNeed(activity, activity)
        }

        override fun onActivityStarted(activity: Activity) {
            applyCustomDensityIfNeed(activity, activity)
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

    private class FragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
            applyCustomDensityIfNeed(f.requireActivity(), f)
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