@file:Suppress("unused")

package com.nice.common.helper

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
import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nice.common.R
import com.nice.common.widget.TitleAppBar
import java.util.regex.Pattern

class NavigationDestination(
    @IdRes val id: Int,
    val className: String,
    val tag: String? = null,
    val label: CharSequence? = null,
    val args: Bundle? = null
) {

    val parent: NavigationGraph?
        get() = _parent

    private var _parent: NavigationGraph? = null

    internal fun setParent(parent: NavigationGraph?) {
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
            return NavigationController(fragmentManager, containerView.id)
        }
        containerView = view.parent as? View
    }
    throw IllegalStateException("Can not create NavigationController for view：$view")
}


private fun getNavigationController(
    fragmentManager: FragmentManager,
    view: View
): NavigationController {
    val controller = view.getTag(R.id.navigation_controller_id) as? NavigationController
    if (controller != null) {
        return controller
    }
    return NavigationController(fragmentManager, view).also {
        view.setTag(R.id.navigation_controller_id, it)
    }
}

fun interface FragmentNavigator {

    fun navigate(
        fragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
        className: String,
        tag: String?,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int,
        allowingStateLoss: Boolean,
        args: () -> Bundle?
    )

}

private object DefaultFragmentNavigator : FragmentNavigator {

    override fun navigate(
        fragmentManager: FragmentManager,
        containerViewId: Int,
        className: String,
        tag: String?,
        enter: Int,
        exit: Int,
        allowingStateLoss: Boolean,
        args: () -> Bundle?
    ) {
        fragmentManager.show(
            containerViewId,
            className,
            tag,
            enter,
            exit,
            allowingStateLoss,
            args
        )
    }

}

class NavigationGraph : Iterable<NavigationDestination> {

    @IdRes
    private var startDestination: Int = -1

    private val destinations = SparseArrayCompat<NavigationDestination>()

    val size: Int
        get() = destinations.size()

    fun addDestination(destination: NavigationDestination) {
        val existingDestination = destinations.get(destination.id)
        if (existingDestination === destination) {
            return
        }
        check(destination.parent == null) {
            "Destination already has a parent set. Call NavigationGraph.remove() to remove the previous parent."
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

}

class NavigationController internal constructor(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerViewId: Int
) {

    private var _fragmentNavigator: FragmentNavigator = DefaultFragmentNavigator
    val fragmentNavigator: FragmentNavigator
        get() = _fragmentNavigator

    private var _graph: NavigationGraph? = null
    val graph: NavigationGraph?
        get() = _graph

    private var primaryNavigationDestination: NavigationDestination? = null

    private val listeners = mutableListOf<OnDestinationChangedListener>()

    fun setFragmentNavigator(navigator: FragmentNavigator) {
        this._fragmentNavigator = navigator
    }

    fun setGraph(graph: NavigationGraph) {
        this._graph = graph
    }

    fun navigate(
        @IdRes id: Int,
        allowingStateLoss: Boolean = false
    ): Boolean {
        val destination = graph?.findDestination(id) ?: return false
        return navigate(destination, allowingStateLoss)
    }

    fun navigate(
        @IdRes id: Int,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int,
        allowingStateLoss: Boolean = false
    ): Boolean {
        val destination = graph?.findDestination(id) ?: return false
        return navigate(destination, enter, exit, allowingStateLoss)
    }

    fun navigate(
        destination: NavigationDestination,
        allowingStateLoss: Boolean = false
    ): Boolean {
        return navigate(destination, R.anim.anim_nav_enter, R.anim.anim_nav_exit, allowingStateLoss)
    }

    fun navigate(
        destination: NavigationDestination,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int,
        allowingStateLoss: Boolean = false
    ): Boolean {
        val parent = destination.parent
        if (parent == null || parent != graph) {
            return false
        }

        setPrimaryNavigationDestination(destination)

        _fragmentNavigator.navigate(
            fragmentManager,
            containerViewId,
            destination.className,
            destination.tag,
            enter,
            exit,
            allowingStateLoss
        ) { destination.args }
        return true
    }

    internal fun setPrimaryNavigationDestination(destination: NavigationDestination?) {
        primaryNavigationDestination = destination

        if (destination != null) {
            for (callback in listeners) {
                callback.onDestinationChanged(this, destination)
            }
        }
    }

    fun getPrimaryNavigationDestination(): NavigationDestination? {
        return primaryNavigationDestination
    }

    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        if (!listeners.contains(listener)) {
            val destination = primaryNavigationDestination
            if (destination != null) {
                listener.onDestinationChanged(this, destination)
            }

            listeners.add(listener)
        }
    }

    fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        listeners.remove(listener)
    }

    fun interface OnDestinationChangedListener {
        fun onDestinationChanged(
            controller: NavigationController,
            destination: NavigationDestination
        )
    }

}

operator fun NavigationGraph.get(@IdRes id: Int): NavigationDestination = requireNotNull(findDestination(id)) {
    "No destination for $id was found in $this"
}

operator fun NavigationGraph.contains(@IdRes id: Int): Boolean = findDestination(id) != null

operator fun NavigationGraph.plusAssign(destination: NavigationDestination) {
    addDestination(destination)
}

operator fun NavigationGraph.plusAssign(destinations: Collection<NavigationDestination>) {
    addDestinations(destinations)
}

operator fun NavigationGraph.minusAssign(node: NavigationDestination) {
    removeDestination(node)
}

fun NavigationController.navigate(
    item: MenuItem,
    allowingStateLoss: Boolean = false
): Boolean = navigate(item.itemId, allowingStateLoss)

fun NavigationController.navigate(
    tab: TabLayout.Tab,
    allowingStateLoss: Boolean = false
): Boolean = navigate(tab.id, allowingStateLoss)

fun BottomNavigationView.setupWithController(
    controller: NavigationController,
    itemConfigurationStrategy: (item: MenuItem, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnItemSelected {
        controller.navigate(it)
    }

    if (itemCount == 0) {
        for ((index, destination) in graph.withIndex()) {
            val item = menu.add(Menu.NONE, destination.id, Menu.NONE, destination.label)
            itemConfigurationStrategy(item, index)
        }
    } else {
        check(itemCount == graph.size) {
            "The number of items in BottomNavigationView and the number of destinations in NavigationGraph are inconsistent"
        }

        for ((index, item) in items.withIndex()) {
            itemConfigurationStrategy(item, index)
        }
    }

    selectedItemId = graph.getStartDestination()
}

fun NavigationView.setupWithController(
    controller: NavigationController,
    itemConfigurationStrategy: (item: MenuItem, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnItemSelected {
        controller.navigate(it)
    }

    if (itemCount == 0) {
        for ((index, destination) in graph.withIndex()) {
            val item = menu.add(Menu.NONE, destination.id, Menu.NONE, destination.label)
            itemConfigurationStrategy(item, index)
        }
    } else {
        check(itemCount == graph.size) {
            "The number of items in NavigationView and the number of destinations in NavigationGraph are inconsistent"
        }

        for ((index, item) in items.withIndex()) {
            itemConfigurationStrategy(item, index)
        }
    }

    checkedItemId = graph.getStartDestination()
}

fun TabLayout.setupWithController(
    controller: NavigationController,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnTabSelected {
        controller.navigate(it)
    }

    if (tabCount == 0) {
        for ((index, destination) in graph.withIndex()) {
            val tab = newTab().setId(destination.id).setText(destination.label)
            tabConfigurationStrategy(tab, index)
            addTab(tab, false)
        }
    } else {
        check(tabCount == graph.size) {
            "The number of tabs in TabLayout and the number of destinations in NavigationGraph are inconsistent"
        }

        for ((index, tab) in tabs.withIndex()) {
            tabConfigurationStrategy(tab, index)
        }
    }

    selectedTabId = graph.getStartDestination()
}

fun AppCompatActivity.setupTabLayoutWithController(
    graph: NavigationGraph,
    tabLayout: TabLayout,
    viewPager: ViewPager,
    autoRefresh: Boolean = true
) {
    viewPager.adapter = FragmentPagerAdapter(this, graph)

    val startDestination = graph.findDestination(graph.getStartDestination())
    if (startDestination != null) {
        viewPager.currentItem = graph.indexOf(startDestination)
    }

    tabLayout.setupWithViewPager(viewPager, autoRefresh)
}

fun Fragment.setupTabLayoutWithController(
    graph: NavigationGraph,
    tabLayout: TabLayout,
    viewPager: ViewPager,
    autoRefresh: Boolean = true
) {
    viewPager.adapter = FragmentPagerAdapter(this, graph)

    val startDestination = graph.findDestination(graph.getStartDestination())
    if (startDestination != null) {
        viewPager.currentItem = graph.indexOf(startDestination)
    }

    tabLayout.setupWithViewPager(viewPager, autoRefresh)
}

private class FragmentPagerAdapter : FragmentStatePagerAdapter {

    private val graph: NavigationGraph

    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader

    constructor(fragmentActivity: FragmentActivity, graph: NavigationGraph) : super(
        fragmentActivity.supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        this.graph = graph
        fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        classLoader = fragmentActivity.classLoader
    }

    constructor(fragment: Fragment, graph: NavigationGraph) : super(
        fragment.childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        this.graph = graph
        fragmentFactory = fragment.childFragmentManager.fragmentFactory
        classLoader = fragment.requireContext().classLoader
    }

    override fun getCount(): Int {
        return graph.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return graph.getDestination(position)?.label
    }

    override fun getItem(position: Int): Fragment {
        val destination = requireNotNull(graph.getDestination(position))
        return fragmentFactory.instantiate(classLoader, destination.className).apply {
            arguments = destination.args
        }
    }

}

fun AppCompatActivity.setupTabLayoutWithController(
    graph: NavigationGraph,
    tabLayout: TabLayout,
    viewPager2: ViewPager2,
    autoRefresh: Boolean = true,
    smoothScroll: Boolean = true,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit = { _, _ -> }
) {
    val startDestination = graph.findDestination(graph.getStartDestination())
    if (startDestination != null) {
        viewPager2.currentItem = graph.indexOf(startDestination)
    }

    viewPager2.adapter = FragmentPagerAdapter2(this, graph)

    TabLayoutMediator(tabLayout, viewPager2, autoRefresh, smoothScroll) { tab, position ->
        tab.text = graph.getDestination(position)?.label
        tabConfigurationStrategy(tab, position)
    }.attach()
}

fun Fragment.setupTabLayoutWithController(
    graph: NavigationGraph,
    tabLayout: TabLayout,
    viewPager2: ViewPager2,
    autoRefresh: Boolean = true,
    smoothScroll: Boolean = true,
    tabConfigurationStrategy: (tab: TabLayout.Tab, index: Int) -> Unit = { _, _ -> }
) {
    val startDestination = graph.findDestination(graph.getStartDestination())
    if (startDestination != null) {
        viewPager2.currentItem = graph.indexOf(startDestination)
    }

    viewPager2.adapter = FragmentPagerAdapter2(this, graph)

    TabLayoutMediator(tabLayout, viewPager2, autoRefresh, smoothScroll) { tab, position ->
        tab.text = graph.getDestination(position)?.label
        tabConfigurationStrategy(tab, position)
    }.attach()
}

private class FragmentPagerAdapter2 :
    FragmentStateAdapter {

    private val graph: NavigationGraph

    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader

    constructor(fragmentActivity: FragmentActivity, graph: NavigationGraph) : super(
        fragmentActivity
    ) {
        this.graph = graph
        fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        classLoader = fragmentActivity.classLoader
    }

    constructor(fragment: Fragment, graph: NavigationGraph) : super(fragment) {
        this.graph = graph
        fragmentFactory = fragment.childFragmentManager.fragmentFactory
        classLoader = fragment.requireContext().classLoader
    }

    override fun getItemCount(): Int {
        return graph.size
    }

    override fun createFragment(position: Int): Fragment {
        val destination = requireNotNull(graph.getDestination(position))
        return fragmentFactory.instantiate(classLoader, destination.className).apply {
            arguments = destination.args
        }
    }

}

fun AppCompatActivity.setupAppBarWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun ActionBar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun Toolbar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun TitleAppBar.setupWithController(controller: NavigationController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

private fun getTitleByDestination(destination: NavigationDestination?): CharSequence? {
    destination ?: return null
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
            title.append(arguments.get(argName))
        }
        matcher.appendTail(title)
        return title
    }
    return null
}