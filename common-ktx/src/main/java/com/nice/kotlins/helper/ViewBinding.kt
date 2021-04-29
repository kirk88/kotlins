@file:Suppress("unused")

package com.nice.kotlins.helper

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
import com.nice.kotlins.app.NiceFragment
import java.lang.reflect.Method

@PublishedApi
internal object ViewBindings {

    private val methods = mutableMapOf<String, Method>()

    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewBinding> inflate(
        clazz: Class<VB>,
        inflater: LayoutInflater,
        parent: ViewGroup? = null,
        attachToParent: Boolean = false
    ) = methods.getOrPut(clazz.name) {
        clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
    }.invoke(null, inflater, parent, attachToParent) as VB

    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewBinding> bind(clazz: Class<VB>, view: View) =
        methods.getOrPut(clazz.name) {
            clazz.getMethod("bind", View::class.java)
        }.invoke(null, view) as VB

}

inline fun <reified VB : ViewBinding> Activity.viewBindings() =
    lazy { viewBinding<VB>(layoutInflater) }

inline fun <reified VB : ViewBinding> Context.viewBindings() =
    lazy { viewBinding<VB>(layoutInflater) }

inline fun <reified VB : ViewBinding> Fragment.viewBindings() =
    FragmentViewBindingLazy(this, VB::class.java)

inline fun <reified VB : ViewBinding> Dialog.viewBindings() =
    lazy { viewBinding<VB>(layoutInflater) }

inline fun <reified VB : ViewBinding> ViewGroup.viewBindings(attachToParent: Boolean = true) =
    lazy { viewBinding<VB>(this, attachToParent) }

inline fun <reified VB : ViewBinding> viewBinding(
    inflater: LayoutInflater,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
) = ViewBindings.inflate(VB::class.java, inflater, parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(
    parent: ViewGroup,
    attachToParent: Boolean = false
) = ViewBindings.inflate(
    VB::class.java,
    LayoutInflater.from(parent.context),
    parent,
    attachToParent
)

inline fun <reified VB : ViewBinding> viewBinding(rootView: View) =
    ViewBindings.bind(VB::class.java, rootView)

inline fun <reified VB : ViewBinding> viewBinding(binding: ViewBinding) =
    ViewBindings.bind(VB::class.java, binding.root)

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

inline fun <reified VB : ViewBinding> bindingView(rootView: View, block: VB.() -> Unit) =
    viewBinding<VB>(rootView).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(binding: ViewBinding, block: VB.() -> Unit) =
    viewBinding<VB>(binding.root).apply(block).root

class FragmentViewBindingLazy<VB : ViewBinding>(
    private val fragment: Fragment,
    private val clazz: Class<VB>
) : Lazy<VB>, Observer<LifecycleOwner>, LifecycleEventObserver {

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, this)
    }

    private var binding: VB? = null

    override val value: VB
        get() {
            val bindingPromise = {
                if (fragment is NiceFragment) {
                    ViewBindings.inflate(clazz, fragment.layoutInflater)
                } else {
                    ViewBindings.bind(clazz, fragment.requireView())
                }
            }
            return binding ?: bindingPromise().also {
                binding = it
            }
        }

    override fun isInitialized(): Boolean = binding != null

    override fun onChanged(owner: LifecycleOwner?) {
        owner ?: return
        fragment.viewLifecycleOwnerLiveData.removeObserver(this)
        owner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            binding = null
        }
    }

}

fun ViewBinding.attachTo(activity: Activity) = activity.setContentView(root)

fun ViewBinding.attachTo(fragment: NiceFragment) = fragment.setContentView(root)

fun ViewBinding.attachTo(parent: ViewGroup) = parent.addView(root)

fun ViewBinding.attachTo(dialog: Dialog) = dialog.setContentView(root)

fun ViewBinding.attachTo(popupWindow: PopupWindow) {
    popupWindow.contentView = root
}