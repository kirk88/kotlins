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
            val view = getViewOrNull<View>(id) ?: continue
            clickViews.add(view)
        }
    }

    fun addOnLongClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = getViewOrNull<View>(id) ?: continue
            longClickViews.add(view)
        }
    }

    fun removeOnClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = getViewOrNull<View>(id) ?: continue
            clickViews.remove(view)
            view.setOnClickListener(null)
        }
    }

    fun removeOnLongClickListener(@IdRes vararg ids: Int) {
        for (id in ids) {
            val view = getViewOrNull<View>(id) ?: continue
            longClickViews.remove(view)
            view.setOnLongClickListener(null)
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

    fun <T : View> getView(@IdRes id: Int): T {
        return getViewOrNull(id)
            ?: throw IllegalArgumentException("Can not find view by id: $id")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> getViewOrNull(@IdRes id: Int): T? {
        val views: SparseArray<View> = (itemView.tag as? SparseArray<View>)
            ?: SparseArray<View>().also { itemView.tag = it }
        var childView: View? = views.get(id)
        if (null == childView) {
            childView = itemView.findViewById(id)
            if (childView == null) {
                return null
            }
            views.put(id, childView)
        }
        return childView as T
    }
}

operator fun <T : View> ItemViewHolder.get(@IdRes id: Int): T = getView(id)

inline fun ItemViewHolder.use(crossinline block: View.() -> Unit) = this.itemView.run(block)