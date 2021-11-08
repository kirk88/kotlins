package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.sample.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nice.common.app.NiceViewModelActivity
import com.nice.common.controller.*
import com.nice.common.event.emitEvent
import com.nice.common.event.emitStickyEvent
import com.nice.common.helper.setContentView
import com.nice.common.helper.viewBindings
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

        val graph = FragmentGraph()
        graph += FragmentDestination(
            R.id.fragment_first,
            FirstFragment::class.java.name,
            label = "First"
        )
        graph += FragmentDestination(
            R.id.fragment_second,
            SecondFragment::class.java.name,
            label = "Second"
        )
        graph += FragmentDestination(
            R.id.fragment_third,
            ThirdFragment::class.java.name,
            label = "Third"
        )

        graph.setStartDestination(R.id.fragment_first)

        val controller = findFragmentController(R.id.frame_container)
        controller.setGraph(graph)

        navView.setupWithFragmentController(controller) { item, position ->
            item.setIcon(
                when (position) {
                    0 -> R.drawable.ic_home_black_24dp
                    1 -> R.drawable.ic_dashboard_black_24dp
                    else -> R.drawable.ic_notifications_black_24dp
                }
            )
        }

        setupAppBarWithFragmentController(controller)

       repeat(3){
           emitStickyEvent("event$it")
       }

        emitEvent("6")
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