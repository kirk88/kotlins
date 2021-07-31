package com.nice.sqlite

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.LinkedSequence
import com.nice.sqlite.core.dml.Projection

class SQLiteColumn<T> internal constructor(
    val column: Projection.Column,
    val type: SqlType
) : Definition {

    private var _meta = Meta<T>()
    val meta: Meta<T> get() = _meta

    fun default(defaultValue: T): SQLiteColumn<T> = apply {
        _meta = _meta.copy(defaultConstraint = ColumnConstraint.Default(defaultValue))
    }

    fun primaryKey(autoIncrement: Boolean = false): SQLiteColumn<T> = apply {
        _meta = _meta.copy(primaryKeyConstraint = ColumnConstraint.PrimaryKey(autoIncrement))
    }

    fun foreignKey(references: Projection.Column): SQLiteColumn<T> = apply {
        _meta = _meta.copy(foreignKeyConstraint = ColumnConstraint.ForeignKey(references))
    }

    fun unique(conflict: Conflict = Conflict.None): SQLiteColumn<T> = apply {
        _meta = _meta.copy(uniqueConstraint = ColumnConstraint.Unique(conflict))
    }

    fun notNull(): SQLiteColumn<T> = apply {
        _meta = _meta.copy(notNullConstraint = ColumnConstraint.NotNull)
    }

    fun onUpdate(actionColumn: ColumnConstraintAction): SQLiteColumn<T> = apply {
        _meta = _meta.copy(onUpdateAction = actionColumn)
    }

    fun onDelete(actionColumn: ColumnConstraintAction): SQLiteColumn<T> = apply {
        _meta = _meta.copy(onDeleteAction = actionColumn)
    }

    override fun render(): String {
        return buildString {
            append(column.render())
            append(' ')
            append(type)
        }
    }

    override fun toString(): String {
        return column.toString()
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

class SQLiteIndex internal constructor(
    val columns: Array<out Projection.Column>,
    val name: String
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun unique(): SQLiteIndex = apply {
        _meta = _meta.copy(unique = IndexConstraint.Unique)
    }

    fun ifNotExists(): SQLiteIndex = apply {
        _meta = _meta.copy(ifNotExists = IndexConstraint.IfNotExists)
    }

    fun ifExists(): SQLiteIndex = apply {
        _meta = _meta.copy(ifExists = IndexConstraint.IfExists)
    }

    override fun render(): String {
        check(columns.isNotEmpty()) {
            "At least 1 column is required to create an index"
        }
        return columns.joinToString(prefix = "(", postfix = ")") {
            it.render()
        }
    }

    override fun toString(): String {
        return columns.joinToString(prefix = "(", postfix = ")")
    }

    data class Meta(
        val unique: IndexConstraint.Unique? = null,
        val ifNotExists: IndexConstraint.IfNotExists? = null,
        val ifExists: IndexConstraint.IfExists? = null
    )

}

class SQLiteDefinitionBuilder @PublishedApi internal constructor() {

    private val definitions = LinkedSequence<Definition>()

    fun define(column: Table.IntColumn): SQLiteColumn<Int> {
        return SQLiteColumn<Int>(column, SqlType.INTEGER).also { definitions.add(it) }
    }

    fun define(column: Table.LongColumn): SQLiteColumn<Long> {
        return SQLiteColumn<Long>(column, SqlType.INTEGER).also { definitions.add(it) }
    }

    fun define(column: Table.ShortColumn): SQLiteColumn<Short> {
        return SQLiteColumn<Short>(column, SqlType.INTEGER).also { definitions.add(it) }
    }

    fun define(column: Table.ByteColumn): SQLiteColumn<Byte> {
        return SQLiteColumn<Byte>(column, SqlType.INTEGER).also { definitions.add(it) }
    }

    fun define(column: Table.BooleanColumn): SQLiteColumn<Boolean> {
        return SQLiteColumn<Boolean>(column, SqlType.INTEGER).also { definitions.add(it) }
    }

    fun define(column: Table.FloatColumn): SQLiteColumn<Float> {
        return SQLiteColumn<Float>(column, SqlType.REAL).also { definitions.add(it) }
    }

    fun define(column: Table.DoubleColumn): SQLiteColumn<Double> {
        return SQLiteColumn<Double>(column, SqlType.REAL).also { definitions.add(it) }
    }

    fun define(column: Table.StringColumn): SQLiteColumn<String> {
        return SQLiteColumn<String>(column, SqlType.TEXT).also { definitions.add(it) }
    }

    fun define(column: Table.BlobColumn): SQLiteColumn<ByteArray> {
        return SQLiteColumn<ByteArray>(column, SqlType.BLOB).also { definitions.add(it) }
    }

    fun define(
        vararg columns: Projection.Column,
        name: String = columns.joinToString("_")
    ): SQLiteIndex {
        return SQLiteIndex(columns, name).also { definitions.add(it) }
    }

    fun build(): Sequence<Definition> = definitions

}