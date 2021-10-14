@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.surrounding

enum class ViewType {
    None,
    Temp,
    Temporary;

    override fun toString(): String =
        if (this == None) "" else name.uppercase()
}

class View(
    val name: String,
    val type: ViewType = ViewType.None
) : Renderer {

    override fun render(): String = name.surrounding()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as View

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = name

}