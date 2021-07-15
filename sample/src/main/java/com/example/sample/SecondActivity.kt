package com.example.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.sample.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nice.kotlins.app.NiceViewModelActivity
import com.nice.kotlins.event.Event
import com.nice.kotlins.helper.*

class SecondActivity : NiceViewModelActivity<TestViewModel>() {

    override val viewModel: TestViewModel by viewModels()

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

        navController.setStartDestination(R.id.fragment_first)

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
    }

    override fun dispatchViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "dispatchViewModelEvent: " + event.message)
        return super.dispatchViewModelEvent(event)
    }

    override fun onInterceptViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "onInterceptViewModelEvent: " + event.message)
        return super.onInterceptViewModelEvent(event)
    }

    override fun onViewModelEvent(event: Event): Boolean {
        Log.e(TAG, "onViewModelEvent: " + event.message)
        return super.onViewModelEvent(event)
    }

    companion object {
        private val TAG = SecondActivity::class.simpleName
    }

}