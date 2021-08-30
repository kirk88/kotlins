@file:Suppress("unused")

package com.nice.common.helper

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.nice.common.app.NiceFragment
import java.lang.reflect.Method

interface ViewBindingFactory {

    fun <VB : ViewBinding> create(viewBindingClass: Class<VB>): VB

}

class ViewBindingInflateFactory(
        private val inflater: LayoutInflater,
        private val parent: ViewGroup? = null,
        private val attachToParent: Boolean = false
) : ViewBindingFactory {
    override fun <VB : ViewBinding> create(viewBindingClass: Class<VB>): VB {
        return ViewBindings.inflate(viewBindingClass, inflater, parent, attachToParent)
    }
}

class ViewBindingBindFactory(private val rootView: View) : ViewBindingFactory {
    override fun <VB : ViewBinding> create(viewBindingClass: Class<VB>): VB {
        return ViewBindings.bind(viewBindingClass, rootView)
    }
}

class ViewBindingLazy<VB : ViewBinding>(
        private val viewBindingClass: Class<VB>,
        private val factoryProducer: () -> ViewBindingFactory
) : Lazy<VB> {

    private var cached: VB? = null

    override val value: VB
        get() = cached ?: factoryProducer().create(viewBindingClass).also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null
}

class FragmentViewBindingLazy<VB : ViewBinding>(
        private val viewBindingClass: Class<VB>,
        private val fragment: Fragment,
        private val factoryProducer: () -> ViewBindingFactory
) : Lazy<VB>, Observer<LifecycleOwner>, LifecycleEventObserver {

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, this)
    }

    private var cached: VB? = null

    override val value: VB
        get() = cached ?: factoryProducer().create(viewBindingClass).also {
            cached = it
        }

    override fun isInitialized(): Boolean = cached != null

    override fun onChanged(owner: LifecycleOwner?) {
        owner ?: return
        fragment.viewLifecycleOwnerLiveData.removeObserver(this)
        owner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            cached = null
        }
    }

}

object ViewBindings {

    private val methods: MutableMap<Class<*>, Method> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewBinding> inflate(
            viewBindingClass: Class<VB>,
            inflater: LayoutInflater,
            parent: ViewGroup? = null,
            attachToParent: Boolean = false
    ) = methods.getOrPut(viewBindingClass) {
        viewBindingClass.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
        )
    }.invoke(null, inflater, parent, attachToParent) as VB

    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewBinding> bind(
        viewBindingClass: Class<VB>,
        rootView: View
    ) = methods.getOrPut(viewBindingClass) {
        viewBindingClass.getMethod("bind", View::class.java)
    }.invoke(null, rootView) as VB

}

val Context.viewBindingFactory: ViewBindingFactory
    get() = ViewBindingInflateFactory(layoutInflater)

val Fragment.viewBindingFactory: ViewBindingFactory
    get() = if (this is NiceFragment) ViewBindingInflateFactory(layoutInflater)
    else ViewBindingBindFactory(requireView())

val Dialog.viewBindingFactory: ViewBindingFactory
    get() = ViewBindingInflateFactory(layoutInflater)

val ViewGroup.viewBindingFactory: ViewBindingFactory
    get() = ViewBindingInflateFactory(layoutInflater, this, true)

inline fun <reified VB : ViewBinding> Activity.viewBindings(noinline factoryProducer: (() -> ViewBindingFactory)? = null): Lazy<VB> {
    val factoryPromise = factoryProducer ?: { viewBindingFactory }
    return ViewBindingLazy(VB::class.java, factoryPromise)
}

inline fun <reified VB : ViewBinding> Context.viewBindings(noinline factoryProducer: (() -> ViewBindingFactory)? = null): Lazy<VB> {
    val factoryPromise = factoryProducer ?: { viewBindingFactory }
    return ViewBindingLazy(VB::class.java, factoryPromise)
}

inline fun <reified VB : ViewBinding> Fragment.viewBindings(noinline factoryProducer: (() -> ViewBindingFactory)? = null): Lazy<VB> {
    val factoryPromise = factoryProducer ?: { viewBindingFactory }
    return FragmentViewBindingLazy(VB::class.java, this, factoryPromise)
}

inline fun <reified VB : ViewBinding> Dialog.viewBindings(noinline factoryProducer: (() -> ViewBindingFactory)? = null): Lazy<VB> {
    val factoryPromise = factoryProducer ?: { viewBindingFactory }
    return ViewBindingLazy(VB::class.java, factoryPromise)
}

inline fun <reified VB : ViewBinding> ViewGroup.viewBindings(factory: ViewBindingFactory? = null): VB {
    return (factory ?: viewBindingFactory).create(VB::class.java)
}

inline fun <reified VB : ViewBinding> viewBinding(
        factory: ViewBindingFactory
) = factory.create(VB::class.java)

inline fun <reified VB : ViewBinding> viewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup? = null,
        attachToParent: Boolean = false
) = ViewBindings.inflate(VB::class.java, inflater, parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(
        parent: ViewGroup,
        attachToParent: Boolean = false
) = ViewBindings.inflate(VB::class.java, parent.layoutInflater, parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(rootView: View) = ViewBindings.bind(VB::class.java, rootView)

inline fun <reified VB : ViewBinding> viewBinding(rootViewBinding: ViewBinding) = ViewBindings.bind(VB::class.java, rootViewBinding.root)

inline fun <reified VB : ViewBinding> bindingView(
        inflater: LayoutInflater,
        parent: ViewGroup? = null,
        attachToParent: Boolean = false,
        block: VB.() -> Unit
): View = viewBinding<VB>(inflater, parent, attachToParent).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(
        parent: ViewGroup,
        attachToParent: Boolean = false,
        block: VB.() -> Unit
): View = viewBinding<VB>(parent, attachToParent).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(rootView: View, block: VB.() -> Unit) = viewBinding<VB>(rootView).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(binding: ViewBinding, block: VB.() -> Unit) = viewBinding<VB>(binding.root).apply(block).root

fun Activity.setContentView(binding: ViewBinding) = setContentView(binding.root)

fun Activity.addContentView(binding: ViewBinding, params: ViewGroup.LayoutParams) = addContentView(binding.root, params)

fun NiceFragment.setContentView(binding: ViewBinding) = setContentView(binding.root)

fun NiceFragment.addContentView(binding: ViewBinding, params: ViewGroup.LayoutParams) = addContentView(binding.root, params)

fun ViewGroup.addView(binding: ViewBinding) = addView(binding.root)

fun Dialog.setContentView(binding: ViewBinding) = setContentView(binding.root)

fun PopupWindow.setContentView(binding: ViewBinding) {
    contentView = binding.root
}