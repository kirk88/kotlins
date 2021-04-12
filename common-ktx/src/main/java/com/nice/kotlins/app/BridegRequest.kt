@file:Suppress("unused")

package com.nice.kotlins.app

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.nice.kotlins.helper.toBundle

class BridegRequest {

    private val bridegFragment: BridegFragment

    constructor(activity: FragmentActivity) {
        bridegFragment = getBridegFragment(activity)
    }

    constructor(fragment: Fragment) {
        bridegFragment = getBridegFragment(fragment.requireActivity())
    }

    private fun getBridegFragment(activity: FragmentActivity): BridegFragment {
        return activity.supportFragmentManager.let {
            it.findFragmentByTag(BRIDEG_TAG) as? BridegFragment
                ?: BridegFragment().also { fragment ->
                    it.beginTransaction().add(fragment, BRIDEG_TAG).commitNowAllowingStateLoss()
                }
        }
    }

    fun startActivityForResult(
        intent: Intent,
        requestCode: Int,
        callback: ActivityResultCallback
    ) {
        bridegFragment.startActivityForResult(intent, requestCode, callback)
    }

    fun requestPermissions(
        vararg permissions: String,
        requestCode: Int,
        callback: PermissionsResultCallback
    ) {
        bridegFragment.requestPermissions(permissions, requestCode, callback)
    }

}

private const val BRIDEG_TAG = "brideg.ActivityResult.BRIDEG_TAG"

inline fun <reified T : Activity> FragmentActivity.startActivityForResult(
    requestCode: Int,
    callback: ActivityResultCallback
) {
    BridegRequest(this).startActivityForResult(
        Intent(this, T::class.java),
        requestCode,
        callback
    )
}

inline fun <reified T : Activity> Fragment.startActivityForResult(
    requestCode: Int,
    callback: ActivityResultCallback
) {
    BridegRequest(this).startActivityForResult(
        Intent(requireContext(), T::class.java),
        requestCode,
        callback
    )
}

inline fun <reified T : Activity> FragmentActivity.startActivityForResult(
    requestCode: Int,
    vararg args: Pair<String, Any?>,
    callback: ActivityResultCallback
) {
    BridegRequest(this).startActivityForResult(Intent(this, T::class.java).apply {
        putExtras(args.toBundle())
    }, requestCode, callback)
}

inline fun <reified T : Activity> Fragment.startActivityForResult(
    requestCode: Int,
    vararg args: Pair<String, Any?>,
    callback: ActivityResultCallback
) {
    BridegRequest(this).startActivityForResult(Intent(requireContext(), T::class.java).apply {
        putExtras(args.toBundle())
    }, requestCode, callback)
}

fun FragmentActivity.requestPermissions(
    vararg permissions: String,
    requestCode: Int,
    callback: PermissionsResultCallback
) {
    BridegRequest(this).requestPermissions(
        requestCode = requestCode,
        callback = callback,
        permissions = permissions
    )
}

fun Fragment.requestPermissions(
    vararg permissions: String,
    requestCode: Int,
    callback: PermissionsResultCallback
) {
    BridegRequest(this).requestPermissions(
        requestCode = requestCode,
        callback = callback,
        permissions = permissions
    )
}