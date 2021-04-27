package com.nice.kotlins.helper

import android.view.MenuItem
import androidx.core.view.children
import androidx.core.view.get
import com.google.android.material.bottomnavigation.BottomNavigationView

val BottomNavigationView.itemCount: Int
    get() = menu.size()

val BottomNavigationView.items: Sequence<MenuItem>
    get() = object : Sequence<MenuItem> {
        override fun iterator() = menu.children.iterator()
    }

fun BottomNavigationView.getItemAt(index: Int): MenuItem? {
    if (index in 0 until menu.size()) {
        return menu[index]
    }
    return null
}

fun BottomNavigationView.requireItemAt(index: Int): MenuItem = requireNotNull(getItemAt(index)) {
    "No item with index $index is found in the BottomNavigationView"
}