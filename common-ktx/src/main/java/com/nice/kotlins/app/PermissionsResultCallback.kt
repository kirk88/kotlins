package com.nice.kotlins.app

fun interface PermissionsResultCallback {

    fun onRequestPermissionsResult(
        requestCode: Int,
        results: Array<Pair<String, Int>>
    )

}