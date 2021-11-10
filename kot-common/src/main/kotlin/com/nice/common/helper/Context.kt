package com.nice.common.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
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
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

val Context.appCompatActivity: AppCompatActivity?
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)