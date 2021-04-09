@file:Suppress("unused")

package com.easy.kotlins.helper

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import com.easy.kotlins.app.NiceFragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified VB : ViewBinding> Activity.viewBindings() =
    lazy { viewBinding<VB>(layoutInflater) }

inline fun <reified VB : ViewBinding> Fragment.viewBindings() =
    FragmentBindingDelegate(VB::class.java)

inline fun <reified VB : ViewBinding> Dialog.viewBindings() =
    lazy { viewBinding<VB>(layoutInflater) }

inline fun <reified VB : ViewBinding> ViewGroup.viewBindings(attachToParent: Boolean = true) =
    lazy { viewBinding<VB>(LayoutInflater.from(context), this, attachToParent) }

inline fun <reified VB : ViewBinding> viewBinding(
    parent: ViewGroup,
    attachToParent: Boolean = false
) = viewBinding<VB>(LayoutInflater.from(parent.context), parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(
    inflater: LayoutInflater,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
) = viewBinding(VB::class.java, inflater, parent, attachToParent)

inline fun <reified VB : ViewBinding> viewBinding(view: View) =
    viewBinding(VB::class.java, view)

fun ViewBinding.installTo(activity: Activity) = activity.setContentView(root)

fun ViewBinding.installTo(fragment: NiceFragment) = fragment.setContentView(root)

fun ViewBinding.installTo(parent: ViewGroup) = parent.addView(root)

fun ViewBinding.installTo(dialog: Dialog) = dialog.setContentView(root)

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

inline fun <reified VB : ViewBinding> withViewBinding(
    inflater: LayoutInflater,
    block: VB.() -> Unit
): View = viewBinding<VB>(inflater).apply(block).root

inline fun <reified VB : ViewBinding> withViewBinding(
    parent: ViewGroup,
    attachToParent: Boolean = false,
    block: VB.() -> Unit
): View = viewBinding<VB>(parent, attachToParent).apply(block).root

inline fun <reified VB : ViewBinding> withViewBinding(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean,
    block: VB.() -> Unit
): View = viewBinding<VB>(inflater, parent, attachToParent).apply(block).root

class FragmentBindingDelegate<VB : ViewBinding>(
    private val clazz: Class<VB>
) : ReadOnlyProperty<Fragment, VB>, LifecycleObserver {

    private var binding: VB? = null

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyView() {
            binding = null
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        if (binding == null) {
            binding = if (thisRef is NiceFragment) {
                viewBinding(clazz, thisRef.layoutInflater)
            } else {
                viewBinding(clazz, thisRef.requireView())
            }

            val lifecycleOwnerLiveData = thisRef.viewLifecycleOwnerLiveData
            val observer = object : Observer<LifecycleOwner> {
                override fun onChanged(owner: LifecycleOwner?) {
                    owner ?: return
                    lifecycleOwnerLiveData.removeObserver(this)

                    owner.lifecycle.addObserver(lifecycleObserver)
                }
            }
            lifecycleOwnerLiveData.observe(thisRef, observer)
        }
        return binding!!
    }

}