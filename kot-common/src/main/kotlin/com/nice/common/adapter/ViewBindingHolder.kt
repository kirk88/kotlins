@file:Suppress("UNUSED")

package com.nice.common.adapter

import androidx.viewbinding.ViewBinding

class ViewBindingHolder<VB : ViewBinding>(val binding: VB) : ItemViewHolder(binding.root)

inline fun <VB : ViewBinding> ViewBindingHolder<VB>.use(crossinline block: VB.() -> Unit) = with(binding, block)