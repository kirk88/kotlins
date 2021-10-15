@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Table

enum class SqlType {
    Integer,
    Real,
    Text,
    Blob;

    override fun toString(): String = name.uppercase()
}

interface Definition : Sequence<Definition>, FullRenderer {

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

    operator fun plus(definition: Definition): MutableSequence<Definition> =
        mutableSequenceOf(this, definition)

}

abstract class Column<T : Any>(
    val table: Table,
    val name: String,
    val type: SqlType
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun default(value: T) = apply {
        _meta = _meta.copy(defaultConstraint = ColumnConstraint.Default(value))
    }

    fun primaryKey(autoIncrement: Boolean = false) = apply {
        _meta = _meta.copy(primaryKeyConstraint = ColumnConstraint.PrimaryKey(autoIncrement))
    }

    fun references(column: Column<*>) = apply {
        _meta = _meta.copy(referencesConstraint = ColumnConstraint.References(column))
    }

    fun unique(conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None) = apply {
        _meta = _meta.copy(uniqueConstraint = ColumnConstraint.Unique(conflictAlgorithm))
    }

    fun notNull() = apply {
        _meta = _meta.copy(notNullConstraint = ColumnConstraint.NotNull)
    }

    fun onUpdate(action: ColumnConstraintAction) = apply {
        _meta = _meta.copy(onUpdateAction = action)
    }

    fun onDelete(action: ColumnConstraintAction) = apply {
        _meta = _meta.copy(onDeleteAction = action)
    }

    override fun render(): String = name.surrounding()

    override fun fullRender(): String = "${table.render()}.${name.surrounding()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column<*>

        if (name != other.name) return false
        if (type != other.type) return false
        if (table != other.table) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + table.hashCode()
        return result
    }

    override fun toString(): String = name

    data class Meta(
        val defaultConstraint: ColumnConstraint.Default? = null,
        val primaryKeyConstraint: ColumnConstraint.PrimaryKey? = null,
        val referencesConstraint: ColumnConstraint.References? = null,
        val uniqueConstraint: ColumnConstraint.Unique? = null,
        val notNullConstraint: ColumnConstraint.NotNull? = null,
        val onUpdateAction: ColumnConstraintAction? = null,
        val onDeleteAction: ColumnConstraintAction? = null
    )

}

class AliasColumn internal constructor(
    val column: Column<*>,
    val alias: String
) : Definition {

    override fun render(): String = "${column.render()} AS ${alias.surrounding()}"

    override fun fullRender(): String = "${column.fullRender()} AS ${alias.surrounding()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AliasColumn

        if (column != other.column) return false
        if (alias != other.alias) return false

        return true
    }

    override fun hashCode(): Int {
        var result = column.hashCode()
        result = 31 * result + alias.hashCode()
        return result
    }

    override fun toString(): String = alias

}

fun aliasColumn(column: Column<*>, alias: String): AliasColumn = AliasColumn(column, alias)

class Index internal constructor(
    val name: String,
    val columns: Array<out Column<*>>
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun unique() = apply {
        _meta = _meta.copy(unique = IndexConstraint.Unique)
    }

    fun ifNotExists() = apply {
        _meta = _meta.copy(ifNotExists = IndexConstraint.IfNotExists)
    }

    fun ifExists() = apply {
        _meta = _meta.copy(ifExists = IndexConstraint.IfExists)
    }

    override fun render(): String = buildString {
        val table = columns.first().table
        append(name.surrounding())
        append(" ON ")
        append(table.render())
    }

    override fun fullRender(): String = buildString {
        val table = columns.first().table
        append(name.surrounding())
        append(" ON ")
        append(table.render())
        append(' ')
        columns.joinTo(this, prefix = "(", postfix = ")") {
            it.render()
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
): Index = Index(name, columns)

class Function internal constructor(private val name: String, private val column: Column<*>) :
    Definition {

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

    override fun render(): String = "$name(${column.render()})"

    override fun fullRender(): String = "$name(${column.fullRender()})"

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