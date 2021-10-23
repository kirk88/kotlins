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

interface Definition : Bag<Definition>, FullRenderer {

    val name: String

    override fun iterator(): Iterator<Definition> = OnceIterator(this)

    operator fun plus(definition: Definition): MutableBag<Definition> =
        mutableBagOf(this, definition)

}

open class Column<T : Any>(
    override val name: String,
    val type: SqlType,
    val table: Table
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun setMeta(
        defaultConstraint: ColumnConstraint.Default? = _meta.defaultConstraint,
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

    operator fun invoke(value: T): Value = Value(this, value)

    operator fun invoke(value: Defined): Value = Value(this, value)

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
        val onUpdateConstraint: ColumnConstraint.OnUpdate? = null,
        val onDeleteConstraint: ColumnConstraint.OnDelete? = null
    )

}

fun <V : Any, T : Column<V>> T.default(value: V) = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

fun <T : Column<*>> T.default(value: Defined) = apply {
    setMeta(defaultConstraint = ColumnConstraint.Default(value))
}

operator fun <T : Column<*>> T.plus(defaultConstraint: ColumnConstraint.Default) = apply {
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

class Index internal constructor(
    override val name: String,
    val columns: Array<out Column<*>>
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun setMeta(
        uniqueConstraint: IndexConstraint.Unique? = _meta.uniqueConstraint,
        ifNotExistsConstraint: IndexConstraint.IfNotExists? = _meta.ifNotExistsConstraint,
        ifExistsConstraint: IndexConstraint.IfExists? = _meta.ifExistsConstraint
    ) {
        _meta = _meta.copy(
            uniqueConstraint = uniqueConstraint,
            ifNotExistsConstraint = ifNotExistsConstraint,
            ifExistsConstraint = ifExistsConstraint
        )
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
        val uniqueConstraint: IndexConstraint.Unique? = null,
        val ifNotExistsConstraint: IndexConstraint.IfNotExists? = null,
        val ifExistsConstraint: IndexConstraint.IfExists? = null
    )

}

fun Index.unique() = apply {
    setMeta(uniqueConstraint = IndexConstraint.Unique)
}

operator fun Index.plus(uniqueConstraint: IndexConstraint.Unique) = apply {
    setMeta(uniqueConstraint = uniqueConstraint)
}

fun Index.ifNotExists() = apply {
    setMeta(ifNotExistsConstraint = IndexConstraint.IfNotExists)
}

operator fun Index.plus(ifNotExistsConstraint: IndexConstraint.IfNotExists) = apply {
    setMeta(ifNotExistsConstraint = ifNotExistsConstraint)
}

fun Index.ifExists() = apply {
    setMeta(ifExistsConstraint = IndexConstraint.IfExists)
}

operator fun Index.plus(ifExistsConstraint: IndexConstraint.IfExists) = apply {
    setMeta(ifExistsConstraint = ifExistsConstraint)
}

fun index(
    vararg columns: Column<*>,
    name: String = columns.joinToString("_")
): Index = Index(name, columns)

sealed class Defined(override val name: String) : Definition {
    override fun render(): String = name
    override fun fullRender(): String = render()
    override fun toString(): String = render()

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

    internal class Named(name: String) : Defined(name)

    internal class Old(private val column: Column<*>) : Defined(column.name) {
        override fun render(): String = "OLD.${column.render()}"
    }

    internal class New(private val column: Column<*>) : Defined(column.name) {
        override fun render(): String = "NEW.${column.render()}"
    }

    object CurrentTime : Defined("CURRENT_TIMESTAMP")
}

fun defined(name: String): Defined = Defined.Named(name)

val Column<*>.old: Defined
    get() = Defined.Old(this)
val Column<*>.new: Defined
    get() = Defined.New(this)

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

fun Column<*>.aliased(name: String) = AliasDefinition(name, this)
fun Function.aliased(name: String) = AliasDefinition(name, this)