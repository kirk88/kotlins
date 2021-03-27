package com.easy.kotlins.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BridegFragment : Fragment() {
    private val activityResultCallbacks: SparseArray<ActivityResultCallback> by lazy {
        SparseArray(5)
    }
    private val permissionsResultCallbacks: SparseArray<PermissionsResultCallback> by lazy {
        SparseArray(5)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun startActivityForResult(
        intent: Intent,
        requestCode: Int,
        callback: ActivityResultCallback
    ) {
        activityResultCallbacks.put(requestCode, callback)
        startActivityForResult(intent, requestCode)
    }

    fun requestPermissions(
        permissions: Array<out String>,
        requestCode: Int,
        callback: PermissionsResultCallback
    ) {
        if (permissions.any {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            permissionsResultCallbacks.put(requestCode, callback)
            requestPermissions(permissions, requestCode)
        } else {
            callback.onRequestPermissionsResult(
                requestCode,
                permissions.map { it to PackageManager.PERMISSION_GRANTED }.toTypedArray()
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = activityResultCallbacks[requestCode]
        activityResultCallbacks.remove(requestCode)
        callback?.onActivityResult(resultCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = permissionsResultCallbacks[requestCode]
        permissionsResultCallbacks.remove(requestCode)
        callback?.onRequestPermissionsResult(
            requestCode,
            permissions.mapIndexed { index, permission ->
                permission to grantResults[index]
            }.toTypedArray()
        )
    }

}


