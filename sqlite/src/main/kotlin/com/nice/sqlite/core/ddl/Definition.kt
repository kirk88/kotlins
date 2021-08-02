@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.dml.MutableSequence
import com.nice.sqlite.core.dml.OnceIterator
import com.nice.sqlite.core.dml.mutableSequenceOf
import com.nice.sqlite.core.render

enum class SqlType {
    INTEGER,
    REAL,
    TEXT,
    BLOB
}

interface Definition : Sequence<Definition>, Renderer {

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

    operator fun plus(definition: Definition): MutableSequence<Definition> =
        mutableSequenceOf(this, definition)

}

abstract class Column<T>(
    val name: String,
    val type: SqlType,
    val table: Table
) : Definition {

    private var _meta = Meta<T>()
    val meta: Meta<T> get() = _meta

    fun default(defaultValue: T): Column<T> = apply {
        _meta = _meta.copy(defaultConstraint = ColumnConstraint.Default(defaultValue))
    }

    fun primaryKey(autoIncrement: Boolean = false): Column<T> = apply {
        _meta = _meta.copy(primaryKeyConstraint = ColumnConstraint.PrimaryKey(autoIncrement))
    }

    fun foreignKey(references: Column<*>): Column<T> = apply {
        _meta = _meta.copy(foreignKeyConstraint = ColumnConstraint.ForeignKey(references))
    }

    fun unique(conflict: Conflict = Conflict.None): Column<T> = apply {
        _meta = _meta.copy(uniqueConstraint = ColumnConstraint.Unique(conflict))
    }

    fun notNull(): Column<T> = apply {
        _meta = _meta.copy(notNullConstraint = ColumnConstraint.NotNull)
    }

    fun onUpdate(actionColumn: ColumnConstraintAction): Column<T> = apply {
        _meta = _meta.copy(onUpdateAction = actionColumn)
    }

    fun onDelete(actionColumn: ColumnConstraintAction): Column<T> = apply {
        _meta = _meta.copy(onDeleteAction = actionColumn)
    }

    override fun render(): String = buildString {
        append(name.render())
        append(' ')
        append(type)
    }

    override fun fullRender(): String = buildString {
        append(table.render())
        append('.')
        append(name.render())
        append(' ')
        append(type)
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column<*>

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }


    data class Meta<T>(
        val defaultConstraint: ColumnConstraint.Default<T>? = null,
        val primaryKeyConstraint: ColumnConstraint.PrimaryKey? = null,
        val foreignKeyConstraint: ColumnConstraint.ForeignKey? = null,
        val uniqueConstraint: ColumnConstraint.Unique? = null,
        val notNullConstraint: ColumnConstraint.NotNull? = null,
        val onUpdateAction: ColumnConstraintAction? = null,
        val onDeleteAction: ColumnConstraintAction? = null
    )

}

class Index internal constructor(
    val columns: Array<out Column<*>>,
    val name: String
) : Definition, Renderer {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun unique(): Index = apply {
        _meta = _meta.copy(unique = IndexConstraint.Unique)
    }

    fun ifNotExists(): Index = apply {
        _meta = _meta.copy(ifNotExists = IndexConstraint.IfNotExists)
    }

    fun ifExists(): Index = apply {
        _meta = _meta.copy(ifExists = IndexConstraint.IfExists)
    }

    override fun render(): String {
        check(columns.isNotEmpty()) {
            "At least 1 column is required to create an index"
        }
        return columns.joinToString(prefix = "(", postfix = ")") {
            it.name.render()
        }
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Index

        if (!columns.contentEquals(other.columns)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columns.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }


    data class Meta(
        val unique: IndexConstraint.Unique? = null,
        val ifNotExists: IndexConstraint.IfNotExists? = null,
        val ifExists: IndexConstraint.IfExists? = null
    )

}

fun index(
    vararg columns: Column<*>,
    name: String = columns.joinToString("_")
): Index = Index(columns, name)

class Function(private val name: String, private val column: Column<*>) : Definition {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Function

        if (name != other.name) return false
        if (column != other.column) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + column.hashCode()
        return result
    }

    override fun render(): String = buildString {
        append(name)
        append('(')
        append(column.name.render())
        append(')')
    }

    override fun fullRender(): String = buildString {
        append(name)
        append('(')
        append(column.table.render())
        append('.')
        append(column.name.render())
        append(')')
    }

    override fun toString(): String = "$name(${column.name})"

}

fun count(column: Column<*>): Definition = Function("count", column)
fun max(column: Column<*>): Definition = Function("max", column)
fun min(column: Column<*>): Definition = Function("min", column)
fun avg(column: Column<*>): Definition = Function("avg", column)
fun sum(column: Column<*>): Definition = Function("sum", column)
fun abs(column: Column<*>): Definition = Function("abs", column)
fun upper(column: Column<*>): Definition = Function("upper", column)
fun lower(column: Column<*>): Definition = Function("lower", column)
fun length(column: Column<*>): Definition = Function("length", column)