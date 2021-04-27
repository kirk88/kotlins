package com.example.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.sample.databinding.ActivitySecondBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.helper.*

class SecondActivity : NiceActivity() {

    private val bindings: ActivitySecondBinding by viewBindings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings.attachTo(this)

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

        navView.setupWithController(navController){ item, position ->
            item.setIcon(when(position){
                0 -> R.drawable.ic_home_black_24dp
                1 -> R.drawable.ic_dashboard_black_24dp
                else -> R.drawable.ic_notifications_black_24dp
            })
        }

        setupAppBarWithController(navController)
    }


}