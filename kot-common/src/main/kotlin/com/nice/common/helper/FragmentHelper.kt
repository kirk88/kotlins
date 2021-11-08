@file:Suppress("UNUSED", "DEPRECATION")

package com.nice.common.controller

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
import com.nice.common.helper.*
import com.nice.common.widget.TitleAppBar
import java.util.regex.Pattern

class FragmentDestination(
    @IdRes val id: Int,
    val className: String,
    val tag: String? = null,
    val label: CharSequence? = null,
    val args: Bundle? = null
) {

    val parent: FragmentGraph?
        get() = _parent

    private var _parent: FragmentGraph? = null

    internal fun setParent(parent: FragmentGraph?) {
        _parent = parent
    }

}

fun AppCompatActivity.findFragmentController(@IdRes id: Int): FragmentController {
    val view = ActivityCompat.requireViewById<View>(this, id)
    return getFragmentController(supportFragmentManager, view)
}

fun Fragment.findFragmentController(@IdRes id: Int): FragmentController {
    val view = ViewCompat.requireViewById<View>(requireView(), id)
    return getFragmentController(childFragmentManager, view)
}

fun FragmentController(fragmentManager: FragmentManager, view: View): FragmentController {
    var containerView: View? = view
    while (containerView != null) {
        if (containerView is FragmentContainerView) {
            return FragmentController(fragmentManager, containerView.id)
        }
        containerView = view.parent as? View
    }
    throw IllegalStateException("Can not create FragmentController for view：$view")
}


private fun getFragmentController(
    fragmentManager: FragmentManager,
    view: View
): FragmentController {
    val controller = view.getTag(R.id.fragment_controller_id) as? FragmentController
    if (controller != null) {
        return controller
    }
    return FragmentController(fragmentManager, view).also {
        view.setTag(R.id.fragment_controller_id, it)
    }
}

fun interface FragmentRouter {

    fun route(
        fragmentManager: FragmentManager,
        @IdRes containerViewId: Int,
        className: String,
        tag: String?,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int,
        allowingStateLoss: Boolean,
        args: Bundle?
    )

}

private object DefaultFragmentRouter : FragmentRouter {

    override fun route(
        fragmentManager: FragmentManager,
        containerViewId: Int,
        className: String,
        tag: String?,
        enter: Int,
        exit: Int,
        allowingStateLoss: Boolean,
        args: Bundle?
    ) {
        fragmentManager.show(
            containerViewId,
            className,
            tag,
            enter,
            exit,
            allowingStateLoss
        ) { args }
    }

}

class FragmentGraph : Iterable<FragmentDestination> {

    @IdRes
    private var startDestination: Int = -1

    private val destinations = SparseArrayCompat<FragmentDestination>()

    val size: Int
        get() = destinations.size()

    fun addDestination(destination: FragmentDestination) {
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

    fun addDestinations(destinations: Collection<FragmentDestination>) {
        for (destination in destinations) {
            addDestination(destination)
        }
    }

    fun addDestinations(vararg destinations: FragmentDestination) {
        for (destination in destinations) {
            addDestination(destination)
        }
    }

    fun findDestination(@IdRes id: Int): FragmentDestination? {
        return destinations[id]
    }

    fun getDestination(index: Int): FragmentDestination? {
        if (index in 0 until destinations.size()) {
            return destinations.valueAt(index)
        }
        return null
    }

    fun removeDestination(destination: FragmentDestination) {
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

    override fun iterator(): MutableIterator<FragmentDestination> {
        return object : MutableIterator<FragmentDestination> {
            private var index = -1
            private var wentToNext = false
            override fun hasNext(): Boolean {
                return index + 1 < destinations.size()
            }

            override fun next(): FragmentDestination {
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

class FragmentController internal constructor(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerViewId: Int
) {

    private var _fragmentRouter: FragmentRouter = DefaultFragmentRouter
    val fragmentRouter: FragmentRouter
        get() = _fragmentRouter

    private var _graph: FragmentGraph? = null
    val graph: FragmentGraph?
        get() = _graph

    private var primaryFragmentDestination: FragmentDestination? = null

    private val listeners = mutableListOf<OnDestinationChangedListener>()

    fun setFragmentHandler(handler: FragmentRouter) {
        this._fragmentRouter = handler
    }

    fun setGraph(graph: FragmentGraph) {
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
        destination: FragmentDestination,
        allowingStateLoss: Boolean = false
    ): Boolean {
        return navigate(destination, R.anim.anim_nav_enter, R.anim.anim_nav_exit, allowingStateLoss)
    }

    fun navigate(
        destination: FragmentDestination,
        @AnimatorRes @AnimRes enter: Int,
        @AnimatorRes @AnimRes exit: Int,
        allowingStateLoss: Boolean = false
    ): Boolean {
        val parent = destination.parent
        if (parent == null || parent != graph) {
            return false
        }

        setPrimaryFragmentDestination(destination)

        fragmentManager.commit {
            add<Fragment>("")
        }

        _fragmentRouter.route(
            fragmentManager,
            containerViewId,
            destination.className,
            destination.tag,
            enter,
            exit,
            allowingStateLoss,
            destination.args
        )
        return true
    }

    internal fun setPrimaryFragmentDestination(destination: FragmentDestination?) {
        primaryFragmentDestination = destination

        if (destination != null) {
            for (callback in listeners) {
                callback.onDestinationChanged(this, destination)
            }
        }
    }

    fun getPrimaryFragmentDestination(): FragmentDestination? {
        return primaryFragmentDestination
    }

    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        if (!listeners.contains(listener)) {
            val destination = primaryFragmentDestination
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
            controller: FragmentController,
            destination: FragmentDestination
        )
    }

}

operator fun FragmentGraph.get(@IdRes id: Int): FragmentDestination = requireNotNull(findDestination(id)) {
    "No destination for $id was found in $this"
}

operator fun FragmentGraph.contains(@IdRes id: Int): Boolean = findDestination(id) != null

operator fun FragmentGraph.plusAssign(destination: FragmentDestination) {
    addDestination(destination)
}

operator fun FragmentGraph.plusAssign(destinations: Collection<FragmentDestination>) {
    addDestinations(destinations)
}

operator fun FragmentGraph.minusAssign(node: FragmentDestination) {
    removeDestination(node)
}

fun FragmentController.route(
    item: MenuItem,
    allowingStateLoss: Boolean = false
): Boolean = navigate(item.itemId, allowingStateLoss)

fun FragmentController.route(
    tab: TabLayout.Tab,
    allowingStateLoss: Boolean = false
): Boolean = navigate(tab.id, allowingStateLoss)

fun BottomNavigationView.setupWithFragmentController(
    controller: FragmentController,
    itemConfigurationStrategy: (item: MenuItem, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnItemSelected {
        controller.route(it)
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

fun NavigationView.setupWithFragmentController(
    controller: FragmentController,
    itemConfigurationStrategy: (item: MenuItem, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnItemSelected {
        controller.route(it)
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

fun TabLayout.setupWithFragmentController(
    controller: FragmentController,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit = { _, _ -> }
) {
    val graph = controller.graph ?: return

    doOnTabSelected {
        controller.route(it)
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

fun AppCompatActivity.setupTabLayoutWithFragmentController(
    graph: FragmentGraph,
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

fun Fragment.setupTabLayoutWithFragmentController(
    graph: FragmentGraph,
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

    private val graph: FragmentGraph

    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader

    constructor(fragmentActivity: FragmentActivity, graph: FragmentGraph) : super(
        fragmentActivity.supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        this.graph = graph
        fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        classLoader = fragmentActivity.classLoader
    }

    constructor(fragment: Fragment, graph: FragmentGraph) : super(
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

fun AppCompatActivity.setupTabLayoutWithFragmentController(
    graph: FragmentGraph,
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

fun Fragment.setupTabLayoutWithFragmentController(
    graph: FragmentGraph,
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

private class FragmentPagerAdapter2 : FragmentStateAdapter {

    private val graph: FragmentGraph

    private val fragmentFactory: FragmentFactory
    private val classLoader: ClassLoader

    constructor(fragmentActivity: FragmentActivity, graph: FragmentGraph) : super(
        fragmentActivity
    ) {
        this.graph = graph
        fragmentFactory = fragmentActivity.supportFragmentManager.fragmentFactory
        classLoader = fragmentActivity.classLoader
    }

    constructor(fragment: Fragment, graph: FragmentGraph) : super(fragment) {
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

fun AppCompatActivity.setupAppBarWithFragmentController(controller: FragmentController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun ActionBar.setupWithFragmentController(controller: FragmentController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun Toolbar.setupWithFragmentController(controller: FragmentController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

fun TitleAppBar.setupWithFragmentController(controller: FragmentController) {
    controller.addOnDestinationChangedListener { _, destination ->
        val title = getTitleByDestination(destination)
        if (title != null) {
            setTitle(title)
        }
    }
}

private fun getTitleByDestination(destination: FragmentDestination?): CharSequence? {
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

private fun FragmentManager.createFragment(
    className: String,
    args: Bundle? = null
): Fragment {
    val classLoader = Thread.currentThread().contextClassLoader
    check(classLoader != null) { "Can not create fragment in current thread: ${Thread.currentThread().name}" }
    return fragmentFactory.instantiate(classLoader, className).apply {
        arguments = args
    }
}

private fun FragmentManager.loadFragment(
    className: String,
    tag: String? = null,
    args: () -> Bundle? = { null }
): Fragment {
    val fragment = findFragmentByTag(tag ?: className)
    if (fragment != null) {
        return fragment
    }
    return createFragment(className, args())
}

private fun FragmentManager.show(
    @IdRes containerViewId: Int,
    className: String,
    tag: String? = null,
    @AnimatorRes @AnimRes enter: Int = 0,
    @AnimatorRes @AnimRes exit: Int = 0,
    allowingStateLoss: Boolean = false,
    args: () -> Bundle? = { null }
): Fragment = loadFragment(className, tag, args).also { fragment ->
    commit(allowingStateLoss) {
        setCustomAnimations(enter, exit)

        for (existingFragment in fragments) {
            if (existingFragment == fragment || (existingFragment.isAdded && existingFragment.id != containerViewId)) {
                continue
            }

            hide(existingFragment)
        }

        if (fragment.isAdded) {
            show(fragment)
        } else {
            add(containerViewId, fragment, tag ?: fragment.javaClass.name)
        }
    }
}