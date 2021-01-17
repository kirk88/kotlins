package com.easy.kotlins.app

import android.content.Intent

fun interface ActivityResultCallback {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

}