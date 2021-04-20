@file:Suppress("unused")

package com.nice.kotlins.app

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class NiceActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId) {

    private var subtitle: CharSequence? = null

    val activityForResultLauncher =
        PocketActivityResultLauncher(ActivityResultContracts.StartActivityForResult())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityForResultLauncher.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityForResultLauncher.unregister()
    }

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)

        supportActionBar?.title = title
    }

    fun setSubtitle(subtitle: CharSequence?) {
        this.subtitle = subtitle

        supportActionBar?.subtitle = subtitle
    }

    fun setSubtitle(subtitleId: Int) {
        setSubtitle(getText(subtitleId))
    }

    fun getSubtitle(): CharSequence? {
        return subtitle
    }

}

var NiceActivity.subtitle: CharSequence?
    get() = getSubtitle()
    set(subtitle) {
        setSubtitle(subtitle)
    }