package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.sample.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nice.common.app.NiceViewModelActivity
import com.nice.common.event.FlowEventBus.emitStickyEvent
import com.nice.common.helper.*
import com.nice.common.viewmodel.Message
import com.nice.common.widget.TipView
import com.nice.common.widget.defaultSnackTipViewFactory
import com.nice.common.widget.tipViews

class SecondActivity : NiceViewModelActivity<TestViewModel>() {

    override val viewModel: TestViewModel by viewModels()

    override val tipView: TipView? by tipViews{ defaultSnackTipViewFactory }

    private val binding: ActivitySecondBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraph = NavigationGraph()
        navGraph += NavigationDestination(
            R.id.fragment_first,
            FirstFragment::class.java.name,
            label = "First"
        )
        navGraph += NavigationDestination(
            R.id.fragment_second,
            SecondFragment::class.java.name,
            label = "Second"
        )
        navGraph += NavigationDestination(
            R.id.fragment_third,
            ThirdFragment::class.java.name,
            label = "Third"
        )

        navGraph.setStartDestination(R.id.fragment_first)

        val navController = findNavigationController(R.id.frame_container)
        navController.setGraph(navGraph)

        navView.setupWithController(navController) { item, position ->
            item.setIcon(
                when (position) {
                    0 -> R.drawable.ic_home_black_24dp
                    1 -> R.drawable.ic_dashboard_black_24dp
                    else -> R.drawable.ic_notifications_black_24dp
                }
            )
        }

        setupAppBarWithController(navController)

        repeat(10){
            emitStickyEvent("event")
        }

    }

    override fun dispatchViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "dispatchViewModelMessage: " + message.javaClass.simpleName)
        return super.dispatchViewModelMessage(message)
    }

    override fun onInterceptViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "onInterceptViewModelMessage: " + message.javaClass.simpleName)
        return super.onInterceptViewModelMessage(message)
    }

    override fun onViewModelMessage(message: Message): Boolean {
        Log.e(TAG, "onViewModelMessage: " + message.javaClass.simpleName)
        return super.onViewModelMessage(message)
    }

    companion object {
        private val TAG = SecondActivity::class.simpleName
    }

}