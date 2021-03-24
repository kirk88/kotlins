package com.easy.kotlins.app

fun interface PermissionsResultCallback {

    fun onRequestPermissionsResult(
        requestCode: Int,
        resultCode: Int,
        results: Array<Pair<String, Int>>
    )

}