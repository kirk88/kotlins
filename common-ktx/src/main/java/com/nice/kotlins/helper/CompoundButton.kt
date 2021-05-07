@file:Suppress("unused")

package com.nice.kotlins.helper

import android.widget.CompoundButton

inline fun CompoundButton.onCheckedChanged(crossinline action: (buttonView: CompoundButton, isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener { buttonView, isChecked -> action(buttonView, isChecked) }
}