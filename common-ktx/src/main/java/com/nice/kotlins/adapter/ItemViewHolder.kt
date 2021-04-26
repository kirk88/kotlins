@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

open class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val views: SparseArray<View> by lazy { SparseArray() }

    private val clickViews: MutableSet<Int> = mutableSetOf()
    private val longClickViews: MutableSet<Int> = mutableSetOf()

    fun addOnClickListener(@IdRes vararg viewIds: Int) {
        clickViews.addAll(viewIds.toList())
    }

    fun addOnLongClickListener(@IdRes vararg viewIds: Int) {
        longClickViews.addAll(viewIds.toList())
    }

    fun removeOnClickListener(@IdRes vararg viewIds: Int) {
        for (id in viewIds) {
            clickViews.remove(id)
            findViewById<View>(id)?.setOnClickListener(null)
        }
    }

    fun removeOnLongClickListener(@IdRes vararg viewIds: Int) {
        for (id in viewIds) {
            longClickViews.remove(id)
            findViewById<View>(id)?.setOnLongClickListener(null)
        }
    }

    fun setOnChildClickListener(clickListener: View.OnClickListener) {
        clickViews.map { id -> findViewById<View>(id) }.forEach {
            it?.setOnClickListener(clickListener)
        }
    }

    fun setOnChildLongClickListener(longClickListener: View.OnLongClickListener) {
        longClickViews.map { id -> findViewById<View>(id) }.forEach {
            it?.setOnLongClickListener(longClickListener)
        }
    }

    fun setOnClickListener(clickListener: View.OnClickListener) {
        itemView.setOnClickListener(clickListener)
    }

    fun setOnLongClickListener(longClickListener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(longClickListener)
    }

    fun <T : View> findViewById(@IdRes id: Int): T? {
        var childView: View? = views.get(id)
        if (null == childView) {
            childView = itemView.findViewById(id)
            if (childView == null) {
                return null
            }
            views.put(id, childView)
        }
        @Suppress("UNCHECKED_CAST")
        return childView as T
    }

}

operator fun <T : View> ItemViewHolder.get(@IdRes id: Int): T = requireNotNull(findViewById(id)) {
    "ID does not reference a View inside the itemView of $this"
}