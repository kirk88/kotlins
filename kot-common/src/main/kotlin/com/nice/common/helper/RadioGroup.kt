@file:Suppress("unused")

package com.nice.common.helper

import android.widget.RadioButton
import android.widget.RadioGroup

inline fun RadioGroup.doOnCheckedChanged(crossinline action: (group: RadioGroup, checkedId: Int) -> Unit) {
    setOnCheckedChangeListener { group, checkedId -> action(group, checkedId) }
}

fun RadioGroup.checkAt(index: Int) {
    val child = getChildAt(index)
    if (child is RadioButton) {
        check(child.id)
    }
}