package com.easy.kotlins.helper

import android.view.View
import android.widget.PopupWindow
import androidx.annotation.IdRes

fun <T : View> PopupWindow.findViewById(@IdRes id: Int): T? = contentView?.findViewById(id)