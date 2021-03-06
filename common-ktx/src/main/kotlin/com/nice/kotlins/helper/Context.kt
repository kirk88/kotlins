package com.nice.kotlins.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity

val Context.application: Application?
    get() {
        var context = this.applicationContext
        while (context is ContextWrapper) {
            if (context is Application) {
                return context
            }
            context = context.baseContext
        }
        return null
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