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

    operator fun plus(value: Any?): SqlColumnElement

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

    override val counter: SqlColumnProperty = SqlFunctionPropertyImpl("count(${this.name})")
    override val maximum: SqlColumnProperty = SqlFunctionPropertyImpl("max(${this.name})")
    override val minimum: SqlColumnProperty = SqlFunctionPropertyImpl("min(${this.name})")
    override val average: SqlColumnProperty = SqlFunctionPropertyImpl("avg(${this.name})")
    override val summation: SqlColumnProperty = SqlFunctionPropertyImpl("sum(${this.name})")
    override val absolute: SqlColumnProperty = SqlFunctionPropertyImpl("abs(${this.name})")
    override val upper: SqlColumnProperty = SqlFunctionPropertyImpl("upper(${this.name})")
    override val lower: SqlColumnProperty = SqlFunctionPropertyImpl("lower(${this.name})")
    override val length: SqlColumnProperty = SqlFunctionPropertyImpl("length(${this.name})")

    override fun render(): String = "$name ${type.render()}"

    override fun plus(value: Any?): SqlColumnElement = SqlColumnElement.create(name, value)

    override fun toString(): String {
        return "SqlColumnProperty(name='$name', type=$type)"
    }

}

private class SqlFunctionPropertyImpl(override val name: String) :
    SqlColumnProperty {

    override val type: SqlType = INTEGER
    override val counter: SqlColumnProperty
        get() = this
    override val maximum: SqlColumnProperty
        get() = this
    override val minimum: SqlColumnProperty
        get() = this
    override val average: SqlColumnProperty
        get() = this
    override val summation: SqlColumnProperty
        get() = this
    override val absolute: SqlColumnProperty
        get() = this
    override val upper: SqlColumnProperty
        get() = this
    override val lower: SqlColumnProperty
        get() = this
    override val length: SqlColumnProperty
        get() = this

    override fun render(): String = "$name ${type.render()}"

    override fun plus(value: Any?): SqlColumnElement = SqlColumnElement.create(name, value)

}

infix fun String.and(type: SqlType): SqlColumnProperty = SqlColumnPropertyImpl(this, type)
infix fun String.and(value: String?): SqlColumnElement = SqlColumnElementImpl(this, value)
infix fun String.and(value: Number?): SqlColumnElement = SqlColumnElementImpl(this, value)