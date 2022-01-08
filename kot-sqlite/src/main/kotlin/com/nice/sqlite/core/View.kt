@file:Suppress("UNUSED")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.QueryStatement
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.addSurrounding

enum class ViewType {
    None,
    Temp,
    Temporary;

    override fun toString(): String =
        if (this == None) "" else name.uppercase()
}

abstract class View(
    val name: String,
    val type: ViewType = ViewType.None
) : Renderer {

    abstract val statement: QueryStatement

    override fun render(): String = name.addSurrounding()

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as View

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

inline fun View(
    name: String,
    type: ViewType = ViewType.None,
    crossinline statement: () -> QueryStatement
) = object : View(name, type){
    override val statement: QueryStatement = statement()
}