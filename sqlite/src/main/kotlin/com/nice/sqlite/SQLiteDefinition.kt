package com.nice.sqlite

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.LinkedSequence
import com.nice.sqlite.core.dml.Projection
import com.nice.sqlite.core.escapedSQLString

class SQLiteColumn internal constructor(
    val column: Projection.Column,
    val type: SqlType,
    val defaultValue: Any?
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun primaryKey(autoIncrement: Boolean = false): SQLiteColumn = apply {
        _meta = _meta.copy(primaryKey = ColumnConstraint.PrimaryKey(autoIncrement))
    }

    fun foreignKey(references: Projection.Column): SQLiteColumn = apply {
        _meta = _meta.copy(foreignKey = ColumnConstraint.ForeignKey(references))
    }

    fun unique(conflict: Conflict = Conflict.None): SQLiteColumn = apply {
        _meta = _meta.copy(unique = ColumnConstraint.Unique(conflict))
    }

    fun notNull(): SQLiteColumn = apply {
        _meta = _meta.copy(notNull = ColumnConstraint.NotNull)
    }

    fun onUpdate(actionColumn: ColumnConstraintAction): SQLiteColumn = apply {
        _meta = _meta.copy(onUpdateAction = actionColumn)
    }

    fun onDelete(actionColumn: ColumnConstraintAction): SQLiteColumn = apply {
        _meta = _meta.copy(onDeleteAction = actionColumn)
    }

    override fun render(): String {
        return buildString {
            append(column.render())
            append(' ')
            append(type)

            if (defaultValue != null) {
                append(" DEFAULT ")
                append(defaultValue.escapedSQLString())
            }
        }
    }

    override fun toString(): String {
        return column.toString()
    }

    data class Meta(
        val primaryKey: ColumnConstraint.PrimaryKey? = null,
        val foreignKey: ColumnConstraint.ForeignKey? = null,
        val unique: ColumnConstraint.Unique? = null,
        val notNull: ColumnConstraint.NotNull? = null,
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

    fun define(column: Table.IntColumn, defaultValue: Int? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.LongColumn, defaultValue: Long? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.ShortColumn, defaultValue: Short? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.ByteColumn, defaultValue: Byte? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.BooleanColumn, defaultValue: Boolean? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.FloatColumn, defaultValue: Float? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.REAL, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.DoubleColumn, defaultValue: Double? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.REAL, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.StringColumn, defaultValue: String? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.TEXT, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.BlobColumn, defaultValue: ByteArray? = null): SQLiteColumn {
        return SQLiteColumn(column, SqlType.BLOB, defaultValue).also { definitions.add(it) }
    }

    fun define(
        vararg columns: Projection.Column,
        name: String = columns.joinToString("_")
    ): SQLiteIndex {
        return SQLiteIndex(columns, name).also { definitions.add(it) }
    }

    fun build(): Sequence<Definition> = definitions

}