@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.surrounding

enum class ViewType {
    None,
    Temp,
    Temporary;

    override fun toString(): String =
        if (this == None) "" else name.uppercase()
}

class View(
    private val name: String,
    val type: ViewType = ViewType.None
) {

    val renderedName: String = name.surrounding()

    override fun toString(): String = name

}