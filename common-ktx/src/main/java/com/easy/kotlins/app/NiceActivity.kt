package com.easy.kotlins.app

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

/**
 * Create by LiZhanPing on 2020/8/24
 */
abstract class NiceActivity(private val layoutID: Int) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutID)
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        onBindData(savedInstanceState)
    }

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onBind(savedInstanceState: Bundle?) {}

    open fun onBindView(savedInstanceState: Bundle?) {}

    open fun onBindEvent(savedInstanceState: Bundle?) {}

    open fun onBindData(savedInstanceState: Bundle?) {}
}