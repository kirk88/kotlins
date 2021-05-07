@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.view.ActionProvider
import androidx.core.view.MenuItemCompat

inline fun MenuItem.onMenuItemClick(crossinline action: (item: MenuItem) -> Boolean) = apply {
    setOnMenuItemClickListener { item ->
        action(item)
    }
}

fun MenuItem.showAsActionAlways() = apply {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
}

fun MenuItem.showAsActionCollapseActionView() = apply {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
}

fun MenuItem.showAsActionIfRoom() = apply {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
}

fun MenuItem.showAsActionNever() = apply {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
}

fun MenuItem.showAsActionWithText() = apply {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT)
}

fun <T : ActionProvider?> MenuItem.getActionProviderCompat(): T {
    @Suppress("UNCHECKED_CAST")
    return MenuItemCompat.getActionProvider(this) as T
}

fun MenuItem.setActionProviderCompat(provider: ActionProvider) {
    MenuItemCompat.setActionProvider(this, provider)
}

fun MenuItem.setIconTintListCompat(color: ColorStateList) {
    MenuItemCompat.setIconTintList(this, color)
}

fun MenuItem.setIconTintCompat(@ColorInt color: Int) {
    MenuItemCompat.setIconTintList(this, ColorStateList.valueOf(color))
}

fun MenuItem.setIconTintModeCompat(mode: PorterDuff.Mode) {
    MenuItemCompat.setIconTintMode(this, mode)
}