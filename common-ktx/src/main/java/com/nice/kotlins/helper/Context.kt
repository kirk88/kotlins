package com.nice.kotlins.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import kotlin.math.sqrt

val Context.application: Application?
    get() {
        var application: Application? = null
        var context = this.applicationContext
        while (context is ContextWrapper) {
            if (context is Application) {
                application = context
                break
            }
            context = context.baseContext
        }
        return application
    }

val Context.activity: Activity?
    get() = scanActivity(this)

private tailrec fun scanActivity(context: Context): Activity? {
    return when (context) {
        is Activity -> context
        is ContextWrapper -> scanActivity(context.baseContext)
        else -> null
    }
}

val Context.appCompatActivity: AppCompatActivity?
    get() = scanCompatActivity(this)

private tailrec fun scanCompatActivity(context: Context?): AppCompatActivity? {
    return when (context) {
        is AppCompatActivity -> context
        is androidx.appcompat.view.ContextThemeWrapper -> scanCompatActivity(context.baseContext)
        is ContextThemeWrapper -> scanCompatActivity(context.baseContext)
        else -> null
    }
}

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.isTabletDevice: Boolean
    get() {
        val dm = Resources.getSystem().displayMetrics
        val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
        val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
        val screenInches = sqrt(x + y)
        return screenInches >= 7.0
    }