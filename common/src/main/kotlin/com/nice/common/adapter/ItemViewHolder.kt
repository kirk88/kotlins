@file:Suppress("unused")

package com.nice.common.adapter

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

open class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val views: SparseArray<View> by lazy { SparseArray() }

    private val clickViews: MutableSet<Int> = mutableSetOf()
    private val longClickViews: MutableSet<Int> = mutableSetOf()

    fun addOnChildClickListener(@IdRes vararg viewIds: Int) {
        clickViews.addAll(viewIds.toList())
    }

    fun addOnChildLongClickListener(@IdRes vararg viewIds: Int) {
        longClickViews.addAll(viewIds.toList())
    }

    fun removeOnChildClickListener(@IdRes vararg viewIds: Int) {
        for (id in viewIds) {
            clickViews.remove(id)
            findViewById<View?>(id)?.setOnClickListener(null)
        }
    }

    fun removeOnChildLongClickListener(@IdRes vararg viewIds: Int) {
        for (id in viewIds) {
            longClickViews.remove(id)
            findViewById<View?>(id)?.setOnLongClickListener(null)
        }
    }

    internal fun setOnChildClickListener(clickListener: View.OnClickListener) {
        clickViews.map { id -> findViewById<View?>(id) }.forEach {
            it?.setOnClickListener(clickListener)
        }
    }

    internal fun setOnChildLongClickListener(longClickListener: View.OnLongClickListener) {
        longClickViews.map { id -> findViewById<View?>(id) }.forEach {
            it?.setOnLongClickListener(longClickListener)
        }
    }

    internal fun setOnClickListener(clickListener: View.OnClickListener) {
        itemView.setOnClickListener(clickListener)
    }

    internal fun removeOnClickListener() {
        itemView.setOnClickListener(null)
    }

    internal fun setOnLongClickListener(longClickListener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(longClickListener)
    }

    internal fun removeOnLongClickListener() {
        itemView.setOnLongClickListener(null)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View?> findViewById(@IdRes id: Int): T {
        var childView: View? = views.get(id)
        if (null == childView) {
            childView = itemView.findViewById(id)
            if (childView != null) {
                views.put(id, childView)
            }
        }
        return childView as T
    }

}

operator fun <T : View> ItemViewHolder.get(@IdRes id: Int): T = requireNotNull(findViewById(id)) {
    "No view with ID $id was found in the itemView of this ViewHolder"
}