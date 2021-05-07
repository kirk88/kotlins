@file:Suppress("unused")

package com.nice.kotlins.helper

import android.os.Build
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun EditText.showIme() {
    val shown = Runnable {
        ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())
    }
    if (Build.VERSION.SDK_INT >= 30) {
        requestFocus()
        post(shown)
    } else {
        shown.run()
    }
}

fun EditText.hideIme() {
    val hidden = Runnable {
        ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())
    }
    if (Build.VERSION.SDK_INT >= 30) {
        requestFocus()
        post(hidden)
    } else {
        hidden.run()
    }
}