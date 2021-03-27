@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

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

    val counter: SqlColumnProperty

    val maximum: SqlColumnProperty

    val minimum: SqlColumnProperty

    val average: SqlColumnProperty

    val summation: SqlColumnProperty

    val absolute: SqlColumnProperty

    val upper: SqlColumnProperty

    val lower: SqlColumnProperty

    val length: SqlColumnProperty

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

private class SqlColumnPropertyImpl(override val name: String, override val type: SqlType) :
    SqlColumnProperty {

    override val counter: SqlColumnProperty = SqlColumnPropertyImpl("count(${this.name})", INTEGER)
    override val maximum: SqlColumnProperty = SqlColumnPropertyImpl("max(${this.name})", INTEGER)
    override val minimum: SqlColumnProperty = SqlColumnPropertyImpl("min(${this.name})", INTEGER)
    override val average: SqlColumnProperty = SqlColumnPropertyImpl("avg(${this.name})", INTEGER)
    override val summation: SqlColumnProperty = SqlColumnPropertyImpl("sum(${this.name})", INTEGER)
    override val absolute: SqlColumnProperty = SqlColumnPropertyImpl("abs(${this.name})", INTEGER)
    override val upper: SqlColumnProperty = SqlColumnPropertyImpl("upper(${this.name})", INTEGER)
    override val lower: SqlColumnProperty = SqlColumnPropertyImpl("lower(${this.name})", INTEGER)
    override val length: SqlColumnProperty = SqlColumnPropertyImpl("length(${this.name})", INTEGER)

    override fun render(): String = "$name ${type.render()}"

    override fun plus(value: Any): SqlColumnElement = SqlColumnElement.create(name, value)

    override fun toString(): String {
        return "SqlColumnProperty(name='$name', type=$type)"
    }

}

infix fun String.and(type: SqlType): SqlColumnProperty = SqlColumnPropertyImpl(this, type)
infix fun String.and(value: String?): SqlColumnElement = SqlColumnElementImpl(this, value)
infix fun String.and(value: Number): SqlColumnElement = SqlColumnElementImpl(this, value)