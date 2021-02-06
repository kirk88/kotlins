package com.easy.kotlins.sqlite

import com.easy.kotlins.sqlite.db.SqlType


interface SqlColumnCell {

    val name: String

    val value: Any?

    companion object {
        fun create(name: String, value: Any?): SqlColumnCell = SqlColumnCellImpl(name, value)
    }
}

interface SqlColumnProperty {

    val name: String

    val type: SqlType

    fun render(): String

    operator fun plus(value: Any): SqlColumnCell

    companion object {
        fun create(name: String, type: SqlType): SqlColumnProperty =
            SqlColumnPropertyImpl(name, type)
    }

}

private open class SqlColumnCellImpl(override val name: String, override val value: Any?) :
    SqlColumnCell

private open class SqlColumnPropertyImpl(override val name: String, override val type: SqlType) :
    SqlColumnProperty {

    override fun render(): String = "$name ${type.render()}"

    override fun plus(value: Any): SqlColumnCell = SqlColumnCell.create(name, value)

}

operator fun String.minus(type: SqlType): SqlColumnProperty = SqlColumnPropertyImpl(this, type)