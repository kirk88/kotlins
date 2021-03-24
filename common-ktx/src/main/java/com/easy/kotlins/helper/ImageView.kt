@file:Suppress("unused")

package com.easy.kotlins.helper

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import coil.load
import java.io.File

var ImageView.imageUrl: String?
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

var ImageView.imageUri: Uri?
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

var ImageView.imageResource: Int
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

var ImageView.imageFile: File?
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

var ImageView.imageDrawable: Drawable?
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

var ImageView.imageBitmap: Bitmap?
    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("No getter")
    set(value) {
        load(value)
    }

fun ImageView.fit(): ImageView = apply { scaleType = ImageView.ScaleType.FIT_XY }

fun ImageView.fitCenter(): ImageView = apply { scaleType = ImageView.ScaleType.FIT_CENTER }

fun ImageView.center(): ImageView = apply { scaleType = ImageView.ScaleType.CENTER }

fun ImageView.centerInside(): ImageView =
    apply { scaleType = ImageView.ScaleType.CENTER_INSIDE }

fun ImageView.centerCrop(): ImageView = apply { scaleType = ImageView.ScaleType.CENTER_CROP }
