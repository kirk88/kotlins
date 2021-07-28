package com.nice.sqlite

import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.LinkedSequence
import com.nice.sqlite.core.dml.Projection

class SQLiteDefinition internal constructor(
    override val column: Projection.Column,
    override val type: SqlType,
    override val defaultValue: Any?
) : Definition {

    private var _meta = Meta()
    val meta: Meta get() = _meta

    fun primaryKey(autoIncrement: Boolean = false): SQLiteDefinition = apply {
        _meta = meta.copy(primaryKeyConstraint = Constraint.PrimaryKey(autoIncrement))
    }

    fun foreignKey(references: Projection.Column): SQLiteDefinition = apply {
        _meta = meta.copy(foreignKeyConstraint = Constraint.ForeignKey(references))
    }

    fun unique(conflict: Conflict = Conflict.None): SQLiteDefinition = apply {
        _meta = meta.copy(uniqueConstraint = Constraint.Unique(conflict))
    }

    fun notNull(): SQLiteDefinition = apply {
        _meta = meta.copy(notNullConstraint = Constraint.NotNull)
    }

    fun onUpdate(action: ConstraintAction): SQLiteDefinition = apply {
        _meta = meta.copy(updateConstraintAction = action)
    }

    fun onDelete(action: ConstraintAction): SQLiteDefinition = apply {
        _meta = meta.copy(deleteConstraintAction = action)
    }

    data class Meta(
        val primaryKeyConstraint: Constraint.PrimaryKey? = null,
        val foreignKeyConstraint: Constraint.ForeignKey? = null,
        val uniqueConstraint: Constraint.Unique? = null,
        val notNullConstraint: Constraint.NotNull? = null,
        val updateConstraintAction: ConstraintAction? = null,
        val deleteConstraintAction: ConstraintAction? = null
    )

}

class SQLiteDefinitionBuilder {

    private val definitions = LinkedSequence<Definition>()

    fun define(column: Table.IntColumn, defaultValue: Int? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.LongColumn, defaultValue: Long? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.ShortColumn, defaultValue: Short? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.ByteColumn, defaultValue: Byte? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.BooleanColumn, defaultValue: Boolean? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.INTEGER, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.FloatColumn, defaultValue: Float? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.REAL, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.DoubleColumn, defaultValue: Double? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.REAL, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.StringColumn, defaultValue: String? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.TEXT, defaultValue).also { definitions.add(it) }
    }

    fun define(column: Table.BlobColumn, defaultValue: ByteArray? = null): SQLiteDefinition {
        return SQLiteDefinition(column, SqlType.BLOB, defaultValue).also { definitions.add(it) }
    }

    fun build(): Sequence<Definition> = definitions

}