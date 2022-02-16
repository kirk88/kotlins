@file:Suppress("UNUSED")

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

interface Definition : Shell<Definition>, FullRenderer {

    val name: String

    override val size: Int get() = 1
    override fun iterator(): Iterator<Definition> = OnceIterator(this)

}

open class Column<T : Any>(
    override val name: String,
    val type: SqlType,
    val table: Table
) : Definition {

    private var _meta = Meta<T>()
    val meta: Meta<T> get() = _meta

    fun setMeta(
        defaultConstraint: ColumnConstraint.Default<T>? = _meta.defaultConstraint,
        primaryKeyConstraint: ColumnConstraint.PrimaryKey? = _meta.primaryKeyConstraint,
        referencesConstraint: ColumnConstraint.References? = _meta.referencesConstraint,
        uniqueConstraint: ColumnConstraint.Unique? = _meta.uniqueConstraint,
        notNullConstraint: ColumnConstraint.NotNull? = _meta.notNullConstraint,
        onUpdateConstraint: ColumnConstraint.OnUpdate? = _meta.onUpdateConstraint,
        onDeleteConstraint: ColumnConstraint.OnDelete? = _meta.onDeleteConstraint
    ) {
        _meta = _meta.copy(
            defaultConstraint = defaultConstraint,
            primaryKeyConstraint = primaryKeyConstraint,
            referencesConstraint = referencesConstraint,
            uniqueConstraint = uniqueConstraint,
            notNullConstraint = notNullConstraint,
            onUpdateConstraint = onUpdateConstraint,
            onDeleteConstraint = onDeleteConstraint
        )
    }

    operator fun invoke(value: T?): ColumnValue = ColumnValue(this, value)

    operator fun invoke(value: Defined): ColumnValue = ColumnValue(this, value)

    override fun render(): String = name.addSurrounding()

    override fun fullRender(): String = "${table.render()}.${name.addSurrounding()}"

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

    data class Meta<T : Any>(
        val defaultConstraint: ColumnConstraint.Default<T>? = null,
        val primaryKeyConstraint: ColumnConstraint.PrimaryKey? = null,
        val referencesConstraint: ColumnConstraint.References? = null,
        val uniqueConstraint: ColumnConstraint.Unique? = null,
        val notNullConstraint: ColumnConstraint.NotNull? = null,
        val onUpdateConstraint: ColumnConstraint.OnUpdate? = null,
        val onDeleteConstraint: ColumnConstraint.OnDelete? = null
    )

}

fun <V : Any, T : Column<V>> T.default(value: V) = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun <V : Any, T : Column<V>> T.default(value: Defined) = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

operator fun <V : Any, T : Column<V>> T.plus(defaultConstraint: ColumnConstraint.Default<V>) = apply {
    setMeta(defaultConstraint = defaultConstraint)
}

fun <T : Column<*>> T.primaryKey(autoIncrement: Boolean = false) = apply {
    setMeta(primaryKeyConstraint = ColumnConstraint.PrimaryKey(autoIncrement))
}

operator fun <V : Any, T : Column<V>> T.plus(primaryKeyConstraint: ColumnConstraint.PrimaryKey?) = apply {
    setMeta(primaryKeyConstraint = primaryKeyConstraint)
}

fun <T : Column<*>> T.references(column: Column<*>) = apply {
    setMeta(referencesConstraint = ColumnConstraint.References(column))
}

operator fun <T : Column<*>> T.plus(referencesConstraint: ColumnConstraint.References) = apply {
    setMeta(referencesConstraint = referencesConstraint)
}

fun <T : Column<*>> T.unique(conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None) = apply {
    setMeta(uniqueConstraint = ColumnConstraint.Unique(conflictAlgorithm))
}

operator fun <T : Column<*>> T.plus(uniqueConstraint: ColumnConstraint.Unique) = apply {
    setMeta(uniqueConstraint = uniqueConstraint)
}

fun <T : Column<*>> T.notNull() = apply {
    setMeta(notNullConstraint = ColumnConstraint.NotNull)
}

operator fun <T : Column<*>> T.plus(notNullConstraint: ColumnConstraint.NotNull) = apply {
    setMeta(notNullConstraint = notNullConstraint)
}

fun <T : Column<*>> T.onUpdate(action: ConstraintAction) = apply {
    setMeta(onUpdateConstraint = ColumnConstraint.OnUpdate(action))
}

operator fun <T : Column<*>> T.plus(onUpdateConstraint: ColumnConstraint.OnUpdate) = apply {
    setMeta(onUpdateConstraint = onUpdateConstraint)
}

fun <T : Column<*>> T.onDelete(action: ConstraintAction) = apply {
    setMeta(onDeleteConstraint = ColumnConstraint.OnDelete(action))
}

operator fun <T : Column<*>> T.plus(onDeleteConstraint: ColumnConstraint.OnDelete) = apply {
    setMeta(onDeleteConstraint = onDeleteConstraint)
}

open class Index(
    override val name: String,
    val unique: Boolean,
    val table: Table,
    val columns: Array<out Column<*>>,
) : Definition {

    override fun render(): String = buildString {
        append(name.addSurrounding())
        append(" ON ")
        append(table.render())
    }

    override fun fullRender(): String = buildString {
        append(name.addSurrounding())
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

}

sealed class Defined(override val name: String) : Definition {
    override fun render(): String = toString()
    override fun fullRender(): String = toString()
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Defined

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    internal class Named(name: String) : Defined(name) {
        override fun toString(): String = name.uppercase()
    }

    internal class Old(private val column: Column<*>) : Defined(column.name) {
        override fun toString(): String = "OLD.${column.render()}"
    }

    internal class New(private val column: Column<*>) : Defined(column.name) {
        override fun toString(): String = "NEW.${column.render()}"
    }
}

fun defined(name: String): Defined = Defined.Named(name)
fun old(column: Column<*>): Defined = Defined.Old(column)
fun new(column: Column<*>): Defined = Defined.New(column)

class Function internal constructor(
    name: String,
    private val values: Array<out Any>
) : Defined(name) {

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

private fun function(
    name: String,
    vararg values: Any
) = Function(name, values)

fun count(column: Column<*>) = function("count", column)
fun max(column: Column<*>) = function("max", column)
fun min(column: Column<*>) = function("min", column)
fun avg(column: Column<*>) = function("avg", column)
fun sum(column: Column<*>) = function("sum", column)
fun abs(column: Column<*>) = function("abs", column)
fun upper(column: Column<*>) = function("upper", column)
fun lower(column: Column<*>) = function("lower", column)
fun length(column: Column<*>) = function("length", column)

fun date(column: Column<*>, vararg modifiers: String) = function("date", column, *modifiers)
fun time(column: Column<*>, vararg modifiers: String) = function("time", column, *modifiers)
fun datetime(column: Column<*>, vararg modifiers: String) = function("datetime", column, *modifiers)
fun date(source: String, vararg modifiers: String) = function("date", source, *modifiers)
fun time(source: String, vararg modifiers: String) = function("time", source, *modifiers)
fun datetime(source: String, vararg modifiers: String) = function("datetime", source, *modifiers)
fun strftime(pattern: String, source: String, vararg modifiers: String) =
    function("strftime", pattern, source, *modifiers)

fun localtime(column: Table.DateColumn): Function = date(column, "localtime")
fun localtime(column: Table.TimeColumn): Function = date(column, "localtime")
fun localtime(column: Table.DatetimeColumn): Function = date(column, "localtime")

class AliasDefinition internal constructor(
    override val name: String,
    val definition: Definition
) : Definition {

    override fun render(): String = "${definition.render()} AS ${name.addSurrounding()}"

    override fun fullRender(): String = "${definition.fullRender()} AS ${name.addSurrounding()}"

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

fun Column<*>.aliased(name: String) = AliasDefinition(name, this)
fun Function.aliased(name: String) = AliasDefinition(name, this)