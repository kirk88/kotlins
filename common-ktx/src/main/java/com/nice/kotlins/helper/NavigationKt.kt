package com.nice.kotlins.helper

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class NavigationController internal constructor(
    val fragmentManager: FragmentManager,
    @IdRes val containerViewId: Int
) : Iterable<NavigationDestination> {

    private val nodes = SparseArrayCompat<NavigationDestination>()

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

    fun NavigationController.navigate(@IdRes id: Int) {
        val destination = getDestination(id) ?: return


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

    fun interface NavigationCallback {
        fun onNavigate(controller: NavigationController, destination: NavigationDestination)
    }

}

fun NavigationController.navigate(activity: AppCompatActivity, @IdRes id: Int) {
    val destination = getDestination(id) ?: return


}

fun NavigationController.navigate(fragment: Fragment, @IdRes id: Int) {
    for (destination in nodes) {
        if (destination.id == id && !callback.onNavigate(destination)) {
            fragment.childFragmentManager.show(
                containerViewId,
                fragment.requireContext(),
                destination.clazzName,
                destination.tag
            ) {
                if (destination.arguments != null) {
                    putAll(destination.arguments)
                }
            }
            break
        }
    }
}

fun AppCompatActivity.setupNavigationViewWithController(
    navView: BottomNavigationView,
    controller: NavigationController
) {
    navView.onItemSelected {
        for (destination in controller.nodes) {
            if (destination.id == it.itemId && !controller.callback.onNavigate(destination)) {
                setTitleWithDestination(destination)

                onNavigationDestinationSelected(
                    destination,
                    controller.containerViewId
                )
                break
            }
        }
        true
    }
}

private fun AppCompatActivity.setTitleWithDestination(destination: NavigationDestination) {
    val label = destination.label
    if (!label.isNullOrBlank()) {
        val arguments = destination.arguments
        val title = StringBuffer()
        val fillInPattern = Pattern.compile("\\{(.+?)\\}")
        val matcher = fillInPattern.matcher(label)
        while (matcher.find()) {
            val argName = matcher.group(1)
            if (arguments != null && arguments.containsKey(argName)) {
                matcher.appendReplacement(title, "")
                title.append(arguments.get(argName).toString())
            } else {
                throw IllegalArgumentException(
                    "Could not find " + argName + " in "
                            + arguments + " to fill label " + label
                )
            }
        }
        matcher.appendTail(title)
        setTitle(title)
    }
}

private fun AppCompatActivity.onNavigationDestinationSelected(
    destination: NavigationDestination,
    @IdRes containerViewId: Int
) {
    supportFragmentManager.show(
        containerViewId,
        this,
        destination.clazzName,
        destination.tag
    ) {
        if (destination.arguments != null) {
            putAll(destination.arguments)
        }
    }
}