package com.nice.kotlins.helper

import android.view.View
import com.google.android.material.tabs.TabLayout

val TabLayout.tabs: Sequence<TabLayout.Tab>
    get() = object : Sequence<TabLayout.Tab> {
        override fun iterator(): Iterator<TabLayout.Tab> {
            val tabs = mutableListOf<TabLayout.Tab>()
            for (index in 0 until tabCount) {
                tabs.add(requireTabAt(index))
            }
            return tabs.iterator()
        }
    }

var TabLayout.selectedTabId: Int
    get() = getTabAt(selectedTabPosition)?.id ?: View.NO_ID
    set(value) {
        tabs.find { it.id == value }?.select()
    }

fun TabLayout.requireTabAt(index: Int): TabLayout.Tab = requireNotNull(getTabAt(index)) {
    "No tab with index $index is found in the TabLayout"
}