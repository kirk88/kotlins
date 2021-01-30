package com.easy.kotlins.adapter

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val clickViews: MutableSet<View> = mutableSetOf()
    private val longClickViews: MutableSet<View> = mutableSetOf()

    fun addOnClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = get<View>(id) ?: continue
            clickViews.add(view)
        }
    }

    fun addOnLongClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = get<View>(id) ?: continue
            longClickViews.add(view)
        }
    }

    fun removeOnClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = get<View>(id) ?: continue
            clickViews.remove(view)
            view.setOnClickListener(null)
        }
    }

    fun removeOnLongClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = get<View>(id) ?: continue
            longClickViews.remove(view)
            view.setOnLongClickListener(null)
        }
    }

    fun removeOnClickListener(vararg views: View) {
        clickViews.removeAll(views)
        views.forEach {
            it.setOnClickListener(null)
        }
    }

    fun removeOnLongClickListener(vararg views: View) {
        longClickViews.removeAll(views)
        views.forEach {
            it.setOnLongClickListener(null)
        }
    }

    fun setOnChildClickListener(clickListener: View.OnClickListener) {
        for (view in clickViews) {
            view.setOnClickListener(clickListener)
        }
    }

    fun setOnChildLongClickListener(longClickListener: View.OnLongClickListener) {
        for (view in longClickViews) {
            view.setOnLongClickListener(longClickListener)
        }
    }

    fun setOnClickListener(clickListener: View.OnClickListener) {
        itemView.setOnClickListener(clickListener)
    }

    fun setOnLongClickListener(longClickListener: View.OnLongClickListener) {
        itemView.setOnLongClickListener(longClickListener)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : View> get(@IdRes id: Int): T? {
        itemView.also {
            val views: SparseArray<View> = (it.tag as? SparseArray<View>
                ?: SparseArray()).apply { it.tag = this }
            var childView: View? = views.get(id)
            if (null == childView) {
                childView = it.findViewById(id)
                if (childView == null) {
                    return null
                }
                views.put(id, childView)
            }
            return childView as T
        }
    }

    fun <T : View> getView(@IdRes id: Int): T {
        return get<T>(id) ?: throw IllegalArgumentException("Can not find view by id: $id")
    }
}