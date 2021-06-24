@file:Suppress("unused")

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

inline fun TabLayout.doOnTabSelected(crossinline action: (tab: TabLayout.Tab) -> Unit) = addOnTabSelectedListener(onTabSelected = action)

inline fun TabLayout.doOnTabUnselected(crossinline action: (tab: TabLayout.Tab) -> Unit) = addOnTabSelectedListener(onTabUnselected = action)

inline fun TabLayout.doOnTabReselected(crossinline action: (tab: TabLayout.Tab) -> Unit) = addOnTabSelectedListener(onTabReselected = action)

inline fun TabLayout.addOnTabSelectedListener(
        crossinline onTabSelected: (tab: TabLayout.Tab) -> Unit = { _ -> },
        crossinline onTabUnselected: (tab: TabLayout.Tab) -> Unit = { _ -> },
        crossinline onTabReselected: (tab: TabLayout.Tab) -> Unit = { _ -> }
): TabLayout.OnTabSelectedListener {
    val listener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onTabSelected.invoke(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            onTabUnselected.invoke(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            onTabReselected.invoke(tab)
        }
    }
    addOnTabSelectedListener(listener)
    return listener
}