package com.pharmacist.base.better.kotlin.app

import android.content.Intent

fun interface ActivityResultCallback {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

}