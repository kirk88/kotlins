package com.nice.kotlins.helper

import androidx.recyclerview.widget.RecyclerView

inline fun RecyclerView.addOnScrollListener(
    crossinline onScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit = { _, _ -> },
    crossinline onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> }
): RecyclerView.OnScrollListener {
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            onScrollStateChanged.invoke(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onScrolled.invoke(recyclerView, dx, dy)
        }
    }
    addOnScrollListener(listener)
    return listener
}

inline fun RecyclerView.doOnScrollStateChanged(crossinline onScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit) =
    addOnScrollListener(onScrollStateChanged = onScrollStateChanged)

inline fun RecyclerView.doOnScrolled(crossinline onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit) =
    addOnScrollListener(onScrolled = onScrolled)