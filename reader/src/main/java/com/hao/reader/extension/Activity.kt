package com.hao.reader.extension

import android.app.Activity
import androidx.core.view.WindowCompat

fun Activity.setDecorFitsSystemWindows(fitsSystemWindow: Boolean) =
    WindowCompat.setDecorFitsSystemWindows(window, fitsSystemWindow)