@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SparseArrayCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.nice.kotlins.R
import java.util.*
import java.util.regex.Pattern

class NavigationDestination(
    @IdRes val id: Int,
    val clazzName: String,
    val tag: String? = null,
    val label: CharSequence? = null,
    val arguments: Bundle? = null
) {
    internal var parent: NavigationController? = null
}

fun AppCompatActivity.findNavigationController(@IdRes id: Int): NavigationController {
    val view = ActivityCompat.requireViewById<View>(this, id)
    return findNavigationController(view, supportFragmentManager)
}

fun Fragment.findNavigationController(@IdRes id: Int): NavigationController {
    val view = ViewCompat.requireViewById<View>(requireView(), id)
    return findNavigationController(view, childFragmentManager)
}

fun findNavigationController(view: View, fragmentManager: FragmentManager): NavigationController {
    val controller = view.getTag(R.id.navigation_controller_tag_id) as? NavigationController
    if (controller != null) {
        return controller
    }
    return NavigationController(fragmentManager, view.context, view.id).also {
        view.setTag(R.id.navigation_controller_tag_id, it)
    }
}

class NavigationController(
    private val fragmentManager: FragmentManager,
    private val context: Context,
    @IdRes private val containerViewId: Int
) : Iterable<NavigationDestination> {

    private val listeners = mutableListOf<OnDestinationChangedListener>()

    private val nodes = SparseArrayCompat<NavigationDestination>()

    private var startDestinationId: Int = -1

    fun addDestination(destination: NavigationDestination) {
        val existingDestination = nodes.get(destination.id)
        if (existingDestination === destination) {
            return
        }
        check(destination.parent == null) {
            "Destination already has a parent set. Call NavGraph.remove() to remove the previous parent."
        }
        if (existingDestination != null) {
            existingDestination.parent = null
        }
        destination.parent = this
        nodes.put(destination.id, destination)
    }

    fun addDestinations(destinations: Collection<NavigationDestination>) {
        for (destination in destinations) {
            addDestination(destination)
        }
    }

    fun addDestinations(vararg destinations: NavigationDestination) {
        for (destination in destinations) {
            addDestination(destination)
        }
    }

    fun getDestination(@IdRes id: Int): NavigationDestination? {
        return nodes[id]
    }

    fun removeDestination(destination: NavigationDestination) {
        val index: Int = nodes.indexOfKey(destination.id)
        if (index >= 0) {
            nodes.valueAt(index).parent = null
            nodes.removeAt(index)
        }
    }

    fun setStartDestination(@IdRes id: Int) {
        startDestinationId = id
    }

    fun getStartDestination(): NavigationDestination? {
        return getDestination(startDestinationId)
    }

    fun navigate(@IdRes id: Int) {
        navigate(this[id])
    }

    fun navigate(
        @IdRes id: Int,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int
    ) {
        navigate(this[id], enter, exit)
    }

    fun navigate(destination: NavigationDestination) {
        navigate(destination, android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun navigate(
        destination: NavigationDestination,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int
    ) {
        val parent = destination.parent
        check(parent != null && parent == this) {
            "Destination not has a parent set yet or it's parent not this NavigationController"
        }

        for (callback in listeners) {
            callback.onDestinationChanged(this, destination)
        }

        fragmentManager.show(
            containerViewId,
            context,
            destination.clazzName,
            destination.tag,
            enter,
            exit
        ) {
            destination.arguments
        }
    }

    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        listeners.remove(listener)
    }

    override fun iterator(): Iterator<NavigationDestination> {
        return object : MutableIterator<NavigationDestination> {
            private var index = -1
            private var wentToNext = false
            override fun hasNext(): Boolean {
                return index + 1 < nodes.size()
            }

            override fun next(): NavigationDestination {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                wentToNext = true
                return nodes.valueAt(++index)
            }

            override fun remove() {
                check(wentToNext) { "You must call next() before you can remove an element" }
                nodes.valueAt(index).parent = null
                nodes.removeAt(index)
                index--
                wentToNext = false
            }
        }
    }

    fun interface OnDestinationChangedListener {
        fun onDestinationChanged(
            controller: NavigationController,
            destination: NavigationDestination
        )
    }

}

operator fun NavigationController.get(@IdRes id: Int): NavigationDestination =
    requireNotNull(getDestination(id)) {
        "No destination for $id was found in $this"
    }

operator fun NavigationController.contains(@IdRes id: Int): Boolean = getDestination(id) != null

operator fun NavigationController.plusAssign(destination: NavigationDestination) {
    addDestination(destination)
}

operator fun NavigationController.plusAssign(destinations: Collection<NavigationDestination>) {
    addDestinations(destinations)
}

operator fun NavigationController.minusAssign(node: NavigationDestination) {
    removeDestination(node)
}

fun NavigationController.navigate(item: MenuItem): Boolean {
    return try {
        navigate(item.itemId)
        true
    } catch (_: Exception) {
        false
    }
}

fun NavigationController.navigate(tab: TabLayout.Tab): Boolean {
    return try {
        navigate(tab.id)
        true
    } catch (_: Exception) {
        false
    }
}

fun AppCompatActivity.setupNavigationViewWithController(
    navView: BottomNavigationView,
    controller: NavigationController
) {
    navView.onItemSelected {
        controller.navigate(it)
    }
    controller.addOnDestinationChangedListener { _, destination ->
        val label = destination.label
        if (!label.isNullOrBlank()) {
            val arguments = destination.arguments
            val title = StringBuffer()
            val fillInPattern = Pattern.compile("\\{(.+?)\\}")
            val matcher = fillInPattern.matcher(label)
            while (matcher.find()) {
                val argName = matcher.group(1)
                check(arguments != null && arguments.containsKey(argName)) {
                    "Could not find $argName in $arguments to fill label $label"
                }
                matcher.appendReplacement(title, "")
                title.append(arguments.get(argName).toString())
            }
            matcher.appendTail(title)
            setTitle(title)
        }
    }
    val destination = controller.getStartDestination()
    if (destination != null) {
        navView.selectedItemId = destination.id
        controller.navigate(destination)
    }
}