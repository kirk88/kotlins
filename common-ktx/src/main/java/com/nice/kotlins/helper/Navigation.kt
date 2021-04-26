@file:Suppress("unused")

package com.nice.kotlins.helper

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.collection.SparseArrayCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isEmpty
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nice.kotlins.R
import com.nice.kotlins.widget.TitleAppBar
import java.util.*
import java.util.regex.Pattern

class NavigationDestination(
    @IdRes val id: Int,
    val clazzName: String,
    val tag: String? = null,
    val label: CharSequence? = null,
    val args: Bundle? = null
) {

    val parent: NavigationController?
        get() = _parent

    private var _parent: NavigationController? = null

    internal fun setParent(parent: NavigationController?) {
        _parent = parent
    }

}

fun AppCompatActivity.findNavigationController(@IdRes id: Int): NavigationController {
    val view = ActivityCompat.requireViewById<View>(this, id)
    return getNavigationController(supportFragmentManager, view)
}

fun Fragment.findNavigationController(@IdRes id: Int): NavigationController {
    val view = ViewCompat.requireViewById<View>(requireView(), id)
    return getNavigationController(childFragmentManager, view)
}

fun NavigationController(fragmentManager: FragmentManager, view: View): NavigationController {
    var containerView: View? = view
    while (containerView != null) {
        if (containerView is FragmentContainerView) {
            return NavigationController(fragmentManager, containerView.context, containerView.id)
        }
        containerView = view.parent as? View
    }
    throw IllegalStateException("Can not create a NavigationController for view：$view")
}


private fun getNavigationController(
    fragmentManager: FragmentManager,
    view: View
): NavigationController {
    val controller = view.getTag(R.id.navigation_controller_tag_id) as? NavigationController
    if (controller != null) {
        return controller
    }
    return NavigationController(fragmentManager, view).also {
        view.setTag(R.id.navigation_controller_tag_id, it)
    }
}

class NavigationController(
    private val fragmentManager: FragmentManager,
    private val context: Context,
    @IdRes private val containerViewId: Int
) : Iterable<NavigationDestination> {

    private val listeners = mutableListOf<OnDestinationChangedListener>()

    private val destinations = SparseArrayCompat<NavigationDestination>()

    @IdRes
    private var startDestination: Int = -1

    fun addDestination(destination: NavigationDestination) {
        val existingDestination = destinations.get(destination.id)
        if (existingDestination === destination) {
            return
        }
        check(destination.parent == null) {
            "Destination already has a parent set. Call NavGraph.remove() to remove the previous parent."
        }
        existingDestination?.setParent(null)
        destination.setParent(this)
        destinations.put(destination.id, destination)
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

    fun findDestination(@IdRes id: Int): NavigationDestination? {
        return destinations[id]
    }

    fun getDestination(index: Int): NavigationDestination? {
        if (index in 0 until destinations.size()) {
            return destinations.valueAt(index)
        }
        return null
    }

    fun removeDestination(destination: NavigationDestination) {
        val index: Int = destinations.indexOfKey(destination.id)
        if (index >= 0) {
            destinations.valueAt(index).setParent(null)
            destinations.removeAt(index)
        }
    }

    fun removeAllDestinations() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }

    fun setStartDestination(@IdRes id: Int) {
        startDestination = id
    }

    @IdRes
    fun getStartDestination(): Int {
        return startDestination
    }

    fun findStartDestination(): NavigationDestination? {
        return findDestination(startDestination)
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
        navigate(destination, R.anim.anim_nav_enter, R.anim.anim_nav_exit)
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
            destination.args
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

    override fun iterator(): MutableIterator<NavigationDestination> {
        return object : MutableIterator<NavigationDestination> {
            private var index = -1
            private var wentToNext = false
            override fun hasNext(): Boolean {
                return index + 1 < destinations.size()
            }

            override fun next(): NavigationDestination {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                wentToNext = true
                return destinations.valueAt(++index)
            }

            override fun remove() {
                check(wentToNext) { "You must call next() before you can remove an element" }
                destinations.valueAt(index).setParent(null)
                destinations.removeAt(index)
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
    requireNotNull(findDestination(id)) {
        "No destination for $id was found in $this"
    }

operator fun NavigationController.contains(@IdRes id: Int): Boolean = findDestination(id) != null

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
    controller: NavigationController,
    itemConfigurationStrategy: (item: MenuItem, position: Int) -> Unit = { _, _ -> }
) {
    navView.onItemSelected {
        controller.navigate(it)
    }

    val menu = navView.menu
    if (menu.isEmpty()) {
        for ((index, destination) in controller.withIndex()) {
            val item = menu.add(Menu.NONE, destination.id, Menu.NONE, destination.label)
            itemConfigurationStrategy(item, index)

            if (destination.id == controller.getStartDestination()) {
                item.isChecked = true
            }
        }
    } else {
        navView.selectedItemId = controller.getStartDestination()
    }
}

fun TabLayout.setupWithController(
    controller: NavigationController,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit = { _, _ -> }
) {
    onTabSelected {
        controller.navigate(it)
    }

    if (tabCount == 0) {
        for ((index, destination) in controller.withIndex()) {
            val tab = newTab().setId(destination.id).setText(destination.label)
            tabConfigurationStrategy(tab, index)
            addTab(tab, false)

            if (destination.id == controller.getStartDestination()) {
                tab.select()
            }
        }
    } else {
        for (index in 0 until tabCount) {
            val tab = getTabAt(index) ?: continue
            if (tab.id == controller.getStartDestination()) {
                tab.select()
                break
            }
        }
    }
}

fun AppCompatActivity.setupTabLayoutWithController(
    tabLayout: TabLayout,
    viewPager2: ViewPager2,
    controller: NavigationController,
    autoRefresh: Boolean = true,
    smoothScroll: Boolean = true,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit = { _, _ -> }
) {
    val startDestination = controller.findStartDestination()
    if (startDestination != null) {
        val index = controller.indexOf(startDestination)
        viewPager2.currentItem = index
    }

    viewPager2.adapter = FragmentPagerAdapter(this, controller)

    TabLayoutMediator(tabLayout, viewPager2, autoRefresh, smoothScroll) { tab, position ->
        tab.text = controller.getDestination(position)?.label
        tabConfigurationStrategy(tab, position)
    }.attach()
}

fun Fragment.setupTabLayoutWithController(
    tabLayout: TabLayout,
    viewPager2: ViewPager2,
    controller: NavigationController,
    autoRefresh: Boolean = true,
    smoothScroll: Boolean = true,
    tabConfigurationStrategy: (tab: TabLayout.Tab, index: Int) -> Unit = { _, _ -> }
) {
    val startDestination = controller.findStartDestination()
    if (startDestination != null) {
        val index = controller.indexOf(startDestination)
        viewPager2.currentItem = index
    }

    viewPager2.adapter = FragmentPagerAdapter(this, controller)

    TabLayoutMediator(tabLayout, viewPager2, autoRefresh, smoothScroll) { tab, position ->
        tab.text = controller.getDestination(position)?.label
        tabConfigurationStrategy(tab, position)
    }.attach()
}

private class FragmentPagerAdapter :
    FragmentStateAdapter {

    private val destinations: List<NavigationDestination>

    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader

    constructor(fragmentActivity: FragmentActivity, controller: NavigationController) : super(
        fragmentActivity
    ) {
        destinations = controller.toList()
        fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        classLoader = fragmentActivity.classLoader
    }

    constructor(fragment: Fragment, controller: NavigationController) : super(fragment) {
        destinations = controller.toList()
        fragmentFactory = fragment.childFragmentManager.fragmentFactory
        classLoader = fragment.requireContext().classLoader
    }


    override fun getItemCount(): Int {
        return destinations.size
    }

    override fun createFragment(position: Int): Fragment {
        val destination = destinations[position]
        return fragmentFactory.instantiate(classLoader, destination.clazzName).apply {
            arguments = destination.args
        }
    }

}

fun AppCompatActivity.setupAppBarWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleFromDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun ActionBar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleFromDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun Toolbar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleFromDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun TitleAppBar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleFromDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

private fun getTitleFromDestination(destination: NavigationDestination): CharSequence? {
    val label = destination.label
    if (!label.isNullOrBlank()) {
        val arguments = destination.args
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
        return title
    }
    return null
}