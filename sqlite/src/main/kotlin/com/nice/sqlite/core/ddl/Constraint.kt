package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.dml.Projection
import com.nice.sqlite.core.escapedSQLString

sealed class ColumnConstraintAction(
    private val name: String
) {
    object SetNull : ColumnConstraintAction("SET NULL")

    object SetDefault : ColumnConstraintAction("SET DEFAULT")

    object SetRestrict : ColumnConstraintAction("SET DEFAULT")

    object Cascade : ColumnConstraintAction("CASCADE")

    object NoAction : ColumnConstraintAction("NO ACTION")

    override fun toString(): String {
        return name
    }
}

sealed class ColumnConstraint {

    class Default<T>(val value: T) : ColumnConstraint() {
        override fun toString(): String = buildString {
            append("DEFAULT ")
            append(value.escapedSQLString())
        }
    }

    class PrimaryKey(val autoIncrement: Boolean) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("PRIMARY KEY")
            if (autoIncrement) {
                append(" AUTOINCREMENT")
            }
        }

    }

    class ForeignKey(val references: Projection.Column) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("REFERENCES ")
            append(references.render(true))
        }

    }

    class Unique(val conflict: Conflict) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("UNIQUE")
            if (conflict != Conflict.None) {
                append(" ON CONFLICT ")
                append(conflict)
            }
        }

    }

    object NotNull : ColumnConstraint() {
        override fun toString(): String = "NOT NULL"
    }

}

sealed class IndexConstraint {

    object Unique : IndexConstraint() {
        override fun toString(): String = "UNIQUE"
    }

    object IfNotExists : IndexConstraint() {
        override fun toString(): String = "IF NOT EXISTS"
    }

    object IfExists : IndexConstraint() {
        override fun toString(): String = "IF EXISTS"
    }

}