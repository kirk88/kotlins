@file:Suppress("unused")

package com.nice.kotlins.helper

import android.app.Activity
import android.content.Context
import android.app.Dialog
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
) = viewBinding(VB::class.java, inflater, parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(
    parent: ViewGroup,
    attachToParent: Boolean = false
) = viewBinding<VB>(LayoutInflater.from(parent.context), parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(rootView: View) =
    viewBinding(VB::class.java, rootView)

inline fun <reified VB : ViewBinding> viewBinding(binding: ViewBinding) =
    viewBinding(VB::class.java, binding.root)

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <VB : ViewBinding> viewBinding(
    clazz: Class<VB>,
    inflater: LayoutInflater,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
) = clazz.getMethod(
    "inflate",
    LayoutInflater::class.java,
    ViewGroup::class.java,
    Boolean::class.java
).invoke(null, inflater, parent, attachToParent) as VB

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <VB : ViewBinding> viewBinding(clazz: Class<VB>, view: View) =
    clazz.getMethod("bind", View::class.java)
        .invoke(null, view) as VB

inline fun <reified VB : ViewBinding> bindingView(
    inflater: LayoutInflater,
    block: VB.() -> Unit
): View = viewBinding<VB>(inflater).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean,
    block: VB.() -> Unit
): View = viewBinding<VB>(inflater, parent, attachToParent).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(
    parent: ViewGroup,
    attachToParent: Boolean = false,
    block: VB.() -> Unit
): View = viewBinding<VB>(parent, attachToParent).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(rootView: View, block: VB.() -> Unit) =
    viewBinding(VB::class.java, rootView).apply(block).root

inline fun <reified VB : ViewBinding> bindingView(binding: ViewBinding, block: VB.() -> Unit) =
    viewBinding(VB::class.java, binding.root).apply(block).root

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
            if (binding == null) {
                binding = if (fragment is NiceFragment) {
                    viewBinding(clazz, fragment.layoutInflater)
                } else {
                    viewBinding(clazz, fragment.requireView())
                }
            }
            return binding!!
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