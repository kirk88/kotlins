@file:Suppress("unused")

package com.nice.kotlins.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import androidx.viewbinding.ViewBinding

class ViewBindingHolder<VB : ViewBinding>(val binding: VB) : ItemViewHolder(binding.root)

inline fun <VB : ViewBinding> ViewBindingHolder<VB>.use(crossinline block: VB.() -> Unit) =
    with(binding, block)