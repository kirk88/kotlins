@file:Suppress("UNUSED")

package com.nice.common.helper

import android.view.MenuItem
import androidx.core.view.get
import androidx.core.view.iterator
import com.google.android.material.navigation.NavigationView

val NavigationView.itemCount: Int
    get() = menu.size()

val NavigationView.items: Sequence<MenuItem>
    get() = object : Sequence<MenuItem> {
        override fun iterator() = menu.iterator()
    }

var NavigationView.checkedItemId: Int
    get() = checkedItem?.itemId ?: 0
    set(value) {
        setCheckedItem(value)
    }

fun NavigationView.getItemAt(index: Int): MenuItem? {
    if (index in 0 until menu.size()) {
        return menu[index]
    }
    return null
}

fun NavigationView.requireItemAt(index: Int): MenuItem = requireNotNull(getItemAt(index)) {
    "No item with index $index is found in the BottomNavigationView"
}

inline fun NavigationView.doOnItemSelected(crossinline action: (item: MenuItem) -> Boolean) = setNavigationItemSelectedListener {
    action(it)
}

