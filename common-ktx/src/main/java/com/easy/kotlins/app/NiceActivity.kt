package com.easy.kotlins.app

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class NiceActivity(@LayoutRes layoutResId: Int = 0) : AppCompatActivity(layoutResId),
    NiceView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBind(savedInstanceState)
        onBindView(savedInstanceState)
        onBindEvent(savedInstanceState)
        onPrepared(savedInstanceState)
        window.decorView.post { onLazyPrepared() }
    }

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}