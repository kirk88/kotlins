package com.easy.kotlins.adapter

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val clickViews: Set<View> by lazy { hashSetOf() }
    val longClickViews: Set<View> by lazy { hashSetOf() }

    fun addOnClickListener(vararg views: View) {
        (clickViews as HashSet<View>).addAll(views)
    }

    fun addOnLongClickListener(vararg views: View) {
        (longClickViews as HashSet<View>).addAll(views)
    }

    fun removeOnClickListener(vararg views: View) {
        (clickViews as HashSet<View>).removeAll(views)
        views.forEach {
            it.setOnClickListener(null)
        }
    }

    fun removeOnLongClickListener(vararg views: View) {
        (longClickViews as HashSet<View>).removeAll(views)
        views.forEach {
            it.setOnLongClickListener(null)
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : View> get(@IdRes viewId: Int): T {
        itemView.also {
            val views: SparseArray<View> = (it.tag as? SparseArray<View>
                ?: SparseArray()).apply { it.tag = this }
            var childView: View? = views.get(viewId)
            if (null == childView) {
                childView = it.findViewById(viewId)
                if (childView == null) {
                    error("can not find view by id: $viewId")
                }
                views.put(viewId, childView)
            }
            return childView as T
        }
    }
}