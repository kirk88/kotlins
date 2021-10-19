@file:Suppress("unused")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Table

sealed class SqlType(val name: String) {
    object Integer : SqlType("integer")
    object Real : SqlType("real")
    object Text : SqlType("text")
    object Blob : SqlType("blob")

    class Named(name: String) : SqlType(name)

    override fun toString(): String = name.uppercase()
}

interface Definition : Sequence<Definition>, FullRenderer {

    val name: String

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

    operator fun plus(definition: Definition): MutableSequence<Definition> =
        mutableSequenceOf(this, definition)

}

class Defined(override val name: String) : Definition {
    override fun render(): String = name
    override fun fullRender(): String = name
    override fun toString(): String = name
}

fun defined(name: String): Defined = Defined(name)

class AliasDefinition internal constructor(
    override val name: String,
    val definition: Definition
) : Definition {

    override fun render(): String = "${definition.render()} AS ${name.surrounding()}"

    override fun fullRender(): String = "${definition.fullRender()} AS ${name.surrounding()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AliasDefinition

        if (definition != other.definition) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = definition.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = name

}

fun Column<*>.aliased(name: String): AliasDefinition = AliasDefinition(name, this)
fun Function.aliased(name: String): AliasDefinition = AliasDefinition(name, this)

abstract class Column<T : Any>(
    override val name: String,
    val type: SqlType,
    val table: Table
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    internal fun setMeta(
        defaultConstraint: ColumnConstraint.Default? = _meta.defaultConstraint,
        primaryKeyConstraint: ColumnConstraint.PrimaryKey? = _meta.primaryKeyConstraint,
        referencesConstraint: ColumnConstraint.References? = _meta.referencesConstraint,
        uniqueConstraint: ColumnConstraint.Unique? = _meta.uniqueConstraint,
        notNullConstraint: ColumnConstraint.NotNull? = _meta.notNullConstraint,
        onUpdateAction: ColumnConstraintAction? = _meta.onUpdateAction,
        onDeleteAction: ColumnConstraintAction? = _meta.onDeleteAction
    ) {
        _meta = _meta.copy(
            defaultConstraint = defaultConstraint,
            primaryKeyConstraint = primaryKeyConstraint,
            referencesConstraint = referencesConstraint,
            uniqueConstraint = uniqueConstraint,
            notNullConstraint = notNullConstraint,
            onUpdateAction = onUpdateAction,
            onDeleteAction = onDeleteAction
        )
    }

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

    operator fun invoke(value: T): Value = Value(this, value)

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

class Index internal constructor(
    override val name: String,
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

    override fun toString(): String = name

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

class Function internal constructor(
    override val name: String,
    private val values: Array<out Any>
) : Definition {

    override fun render(): String = buildString {
        append(name)
        values.joinTo(this, prefix = "(", postfix = ")") {
            if (it is Column<*>) it.render() else it.toSqlString()
        }
    }

    override fun fullRender(): String = buildString {
        append(name)
        values.joinTo(this, prefix = "(", postfix = ")") {
            if (it is Column<*>) it.fullRender() else it.toSqlString()
        }
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Function

        if (name != other.name) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }

}

fun function(
    name: String,
    vararg values: Any
): Function = Function(name, values)

fun count(column: Column<*>): Function = function("count", column)
fun max(column: Column<*>): Function = function("max", column)
fun min(column: Column<*>): Function = function("min", column)
fun avg(column: Column<*>): Function = function("avg", column)
fun sum(column: Column<*>): Function = function("sum", column)
fun abs(column: Column<*>): Function = function("abs", column)
fun upper(column: Column<*>): Function = function("upper", column)
fun lower(column: Column<*>): Function = function("lower", column)
fun length(column: Column<*>): Function = function("length", column)

fun date(column: Column<*>, vararg modifiers: String): Function =
    function("date", column, *modifiers)

fun time(column: Column<*>, vararg modifiers: String): Function =
    function("time", column, *modifiers)

fun datetime(column: Column<*>, vararg modifiers: String): Function =
    function("datetime", column, *modifiers)

fun strftime(column: Column<*>, vararg modifiers: String): Function =
    function("strftime", column, *modifiers)

fun date(source: String, vararg modifiers: String): Function =
    function("date", source, *modifiers)

fun time(source: String, vararg modifiers: String): Function =
    function("time", source, *modifiers)

fun datetime(source: String, vararg modifiers: String): Function =
    function("datetime", source, *modifiers)

fun strftime(pattern: String, source: String, vararg modifiers: String): Function =
    function("strftime", pattern, source, *modifiers)