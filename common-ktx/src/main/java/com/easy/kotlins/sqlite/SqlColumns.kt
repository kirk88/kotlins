package com.easy.kotlins.sqlite

import com.easy.kotlins.sqlite.db.SqlType


interface SqlColumnElement {

    val name: String

    val value: Any?

    companion object {
        fun create(name: String, value: Any?): SqlColumnElement = SqlColumnElementImpl(name, value)
    }

}

interface SqlColumnProperty {

    val name: String

    val type: SqlType

    fun render(): String

    operator fun plus(value: Any): SqlColumnElement

    companion object {
        fun create(name: String, type: SqlType): SqlColumnProperty =
            SqlColumnPropertyImpl(name, type)
    }

}

private open class SqlColumnElementImpl(override val name: String, override val value: Any?) :
    SqlColumnElement {

    override fun toString(): String {
        return "SqlColumnElement(name='$name', value=$value)"
    }
}

private open class SqlColumnPropertyImpl(override val name: String, override val type: SqlType) :
    SqlColumnProperty {

    override fun render(): String = "$name ${type.render()}"

    override fun plus(value: Any): SqlColumnElement = SqlColumnElement.create(name, value)

    override fun toString(): String {
        return "SqlColumnProperty(name='$name', type=$type)"
    }

}

infix fun String.and(type: SqlType): SqlColumnProperty = SqlColumnPropertyImpl(this, type)
infix fun String.and(value: String?): SqlColumnElement = SqlColumnElementImpl(this, value)
infix fun String.and(value: Number): SqlColumnElement = SqlColumnElementImpl(this, value)