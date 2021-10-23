@file:Suppress("UNUSED")

package com.nice.common.helper

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import coil.load
import com.nice.common.external.NO_GETTER
import com.nice.common.external.NO_GETTER_MESSAGE
import java.io.File

var ImageView.imageUrl: String?
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

var ImageView.imageUri: Uri?
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

var ImageView.imageResource: Int
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

var ImageView.imageFile: File?
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

var ImageView.imageDrawable: Drawable?
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

var ImageView.imageBitmap: Bitmap?
    @Deprecated(NO_GETTER_MESSAGE, level = DeprecationLevel.ERROR) get() = NO_GETTER
    set(value) {
        load(value)
    }

fun ImageView.fit(): ImageView = apply { scaleType = ImageView.ScaleType.FIT_XY }

fun ImageView.fitCenter(): ImageView = apply { scaleType = ImageView.ScaleType.FIT_CENTER }

fun ImageView.center(): ImageView = apply { scaleType = ImageView.ScaleType.CENTER }

fun ImageView.centerInside(): ImageView = apply { scaleType = ImageView.ScaleType.CENTER_INSIDE }

fun ImageView.centerCrop(): ImageView = apply { scaleType = ImageView.ScaleType.CENTER_CROP }
