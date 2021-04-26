@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.content.Context
import okhttp3.internal.toImmutableList
import java.util.*

abstract class CommonRecyclerAdapter<T, VH : ItemViewHolder>(context: Context) :
    BaseRecyclerAdapter<T, VH>(context) {

    override val items: List<T>
        get() = modifiableItems.toImmutableList()

    protected val modifiableItems: MutableList<T> = mutableListOf()

    private val lock: Any = Any()

    fun setItems(items: List<T>?) {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                modifiableItems.clear()
            }
            if (items != null) {
                modifiableItems.addAll(items)
            }
            notifyDataSetChanged()
        }
    }

    fun addItem(item: T) {
        synchronized(lock) {
            val oldSize = modifiableItems.size
            if (modifiableItems.add(item)) {
                notifyItemInserted(oldSize)
            }
        }
    }

    fun addItem(position: Int, item: T) {
        synchronized(lock) {
            if (position >= 0) {
                modifiableItems.add(position, item)
                notifyItemInserted(position)
            }
        }
    }

    fun addItems(position: Int, items: List<T>) {
        synchronized(lock) {
            if (modifiableItems.addAll(position, items)) {
                notifyItemRangeInserted(position, items.size)
            }
        }
    }

    fun addItems(items: List<T>) {
        synchronized(lock) {
            val oldSize = modifiableItems.size
            if (modifiableItems.addAll(items)) {
                if (oldSize == 0) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeInserted(oldSize, items.size)
                }
            }
        }
    }

    fun removeItem(position: Int) {
        synchronized(lock) {
            if (modifiableItems.removeAt(position) != null) {
                notifyItemRemoved(position)
            }
        }
    }

    fun removeItem(item: T) {
        synchronized(lock) {
            val position = modifiableItems.indexOf(item)
            if (position >= 0) {
                modifiableItems.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun removeItems(items: List<T>) {
        synchronized(lock) {
            if (modifiableItems.removeAll(items)) {
                notifyDataSetChanged()
            }
        }
    }

    fun updateItem(item: T, payload: Any? = null) {
        synchronized(lock) {
            val position = modifiableItems.indexOf(item)
            if (position >= 0) {
                modifiableItems[position] = item
                notifyItemChanged(position, payload)
            }
        }
    }

    fun updateItems(items: List<T>, payload: Any? = null) {
        synchronized(lock) {
            for (item in items) {
                val position = modifiableItems.indexOf(item)
                if (position >= 0) {
                    modifiableItems[position] = item
                    notifyItemChanged(position, payload)
                }
            }
        }
    }

    fun swapItem(oldPosition: Int, newPosition: Int) {
        synchronized(lock) {
            if (oldPosition in 0 until modifiableItems.size && newPosition in 0 until modifiableItems.size) {
                Collections.swap(modifiableItems, oldPosition, newPosition)
                notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)
            }
        }
    }

    fun sortItems(comparator: Comparator<T>) {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                modifiableItems.sortWith(comparator)
                notifyDataSetChanged()
            }
        }
    }

    fun <R : Comparable<R>> sortItems(selector: (T) -> R?) {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                modifiableItems.sortBy(selector)
                notifyDataSetChanged()
            }
        }
    }

    fun reverseItems() {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                modifiableItems.reverse()
                notifyDataSetChanged()
            }
        }
    }

    fun refreshItems(payload: Any? = null) {
        notifyItemRangeChanged(0, modifiableItems.size, payload)
    }

    fun clearItems() {
        synchronized(lock) {
            modifiableItems.clear()
            notifyDataSetChanged()
        }
    }

}

operator fun <T, VH : ItemViewHolder> CommonRecyclerAdapter<T, *>.plusAssign(item: T) {
    addItem(item)
}

operator fun <T> CommonRecyclerAdapter<T, *>.plusAssign(items: Iterable<T>) {
    if (items is List<*>) {
        setItems(items as List<T>)
    } else {
        setItems(items.toList())
    }
}

operator fun <T> CommonRecyclerAdapter<T, *>.plusAssign(items: Array<T>) {
    setItems(items.toList())
}


operator fun <T, VH : ItemViewHolder> CommonRecyclerAdapter<T, *>.minusAssign(item: T) {
    removeItem(item)
}

operator fun <T, VH : ItemViewHolder> CommonRecyclerAdapter<T, *>.minusAssign(items: Iterable<T>) {
    if (items is List<*>) {
        removeItems(items as List<T>)
    } else {
        removeItems(items.toList())
    }
}

operator fun <T, VH : ItemViewHolder> CommonRecyclerAdapter<T, *>.minusAssign(items: Array<T>) {
    removeItems(items.toList())
}