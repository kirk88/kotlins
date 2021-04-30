@file:Suppress("unused")

package com.nice.kotlins.helper

import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun EditText.showIme(){
    requestFocus()
    post {
        ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())
    }
}

fun EditText.hideIme(){
    clearFocus()
    post {
        ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())
    }
}