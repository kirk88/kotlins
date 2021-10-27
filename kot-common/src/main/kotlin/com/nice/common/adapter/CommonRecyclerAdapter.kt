@file:Suppress("UNUSED", "NotifyDataSetChanged")

package com.nice.common.adapter

import android.content.Context
import java.util.*

abstract class CommonRecyclerAdapter<T, VH : ItemViewHolder>(context: Context, items: List<T>? = null) :
    BaseRecyclerAdapter<T, VH>(context) {

    private val lock: Any = Any()

    protected val modifiableItems: MutableList<T> = items?.toMutableList() ?: mutableListOf()

    override val items: List<T> = Collections.unmodifiableList(modifiableItems)

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
        itemAnimation?.setStartPosition(-1)
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
                notifyItemRangeInserted(oldSize, items.size)
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

    fun removeItemAt(position: Int) {
        synchronized(lock) {
            if (modifiableItems.removeAt(position) != null) {
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

    fun updateItemAt(position: Int, item: T, payload: Any? = null) {
        synchronized(lock) {
            modifiableItems[position] = item
            notifyItemChanged(position, payload)
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

    fun sortItemsWith(comparator: Comparator<T>) {
        synchronized(lock) {
            if (modifiableItems.isNotEmpty()) {
                modifiableItems.sortWith(comparator)
                notifyDataSetChanged()
            }
        }
    }

    fun <R : Comparable<R>> sortItemsBy(selector: (T) -> R?) {
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

    fun clearItems() {
        synchronized(lock) {
            modifiableItems.clear()
            notifyDataSetChanged()
        }
    }

    fun indexOfItem(item: T): Int {
        val index: Int
        synchronized(lock) {
            index = modifiableItems.indexOf(item)
        }
        return index
    }

    fun containsItem(item: T): Boolean = indexOfItem(item) > -1

    fun refreshItems(payload: Any? = null) {
        notifyItemRangeChanged(0, modifiableItems.size, payload)
    }

}

operator fun <T> CommonRecyclerAdapter<T, *>.plusAssign(items: List<T>?) = setItems(items)

operator fun <T> CommonRecyclerAdapter<T, *>.plus(item: T) = addItem(item)

operator fun <T> CommonRecyclerAdapter<T, *>.plus(items: List<T>) = addItems(items)

operator fun <T> CommonRecyclerAdapter<T, *>.minus(item: T) = removeItem(item)

operator fun <T> CommonRecyclerAdapter<T, *>.minus(items: List<T>) = removeItems(items)