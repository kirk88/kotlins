package com.easy.kotlins.helper

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment

/**
 * Create by LiZhanPing on 2020/10/20
 * desc:
 */

fun Activity.findNavController(@IdRes viewId: Int): NavController {
    return Navigation.findNavController(this, viewId)
}

fun Fragment.findNavController(): NavController {
    return NavHostFragment.findNavController(this)
}

fun Fragment.findChildNavController(@IdRes viewId: Int): NavController {
    return Navigation.findNavController(ViewCompat.requireViewById(requireNotNull(view), viewId))
}

fun View.findNavController(): NavController {
    return Navigation.findNavController(this)
}