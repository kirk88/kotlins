package com.nice.kotlins.helper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity

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