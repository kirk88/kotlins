package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nice.bluetooth.Bluetooth
import com.nice.common.app.NiceViewModelActivity
import com.nice.common.event.Message
import com.nice.common.helper.*
import com.nice.common.widget.TipView
import com.nice.common.widget.tipViews
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SecondActivity : NiceViewModelActivity<TestViewModel>() {

    override val viewModel: TestViewModel by viewModels()

    override val tipView: TipView? by tipViews()

    private val binding: ActivitySecondBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navController = findNavigationController(R.id.frame_container)
        navController += NavigationDestination(
            R.id.fragment_first,
            FirstFragment::class.java.name,
            label = "First"
        )
        navController += NavigationDestination(
            R.id.fragment_second,
            SecondFragment::class.java.name,
            label = "Second"
        )
        navController += NavigationDestination(
            R.id.fragment_third,
            ThirdFragment::class.java.name,
            label = "Third"
        )

        navController.setStartDestination(R.id.fragment_second)

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

        Bluetooth.state.onEach {
            Log.e(TAG, "state: $it")
        }.launchIn(lifecycleScope)

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