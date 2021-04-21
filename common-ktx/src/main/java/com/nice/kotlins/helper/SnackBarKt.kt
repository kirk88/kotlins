package com.nice.kotlins.helper

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this, message, duration)

fun Fragment.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this.requireView(), message, duration)

fun Activity.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this.window.decorView, message, duration)


fun View.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this, message, duration)

fun Fragment.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this.requireView(), message, duration)

fun Activity.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar =
    Snackbar.make(this.window.decorView, message, duration)


fun View.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun Fragment.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this.requireView(), message, duration).show()
}

fun Activity.showSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this.window.decorView, message, duration).show()
}

fun View.showSnackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun Fragment.showSnackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this.requireView(), message, duration).show()
}

fun Activity.showSnackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this.window.decorView, message, duration).show()
}
