@file:Suppress("unused")

package com.nice.sqlite.core.ddl

sealed class ColumnConstraintAction(
    private val name: String
) {
    object SetNull : ColumnConstraintAction("SET NULL")
    object SetDefault : ColumnConstraintAction("SET DEFAULT")
    object SetRestrict : ColumnConstraintAction("SET RESTRICT")
    object Cascade : ColumnConstraintAction("CASCADE")
    object NoAction : ColumnConstraintAction("NO ACTION")

    override fun toString(): String = name
}

sealed class ColumnConstraint {

    class Default(val value: Any) : ColumnConstraint() {
        override fun toString(): String = buildString {
            append("DEFAULT ")
            if (value is Function) {
                append('(')
                append(value.toSqlString())
                append(')')
            } else {
                append(value.toSqlString())
            }
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

    class References(val column: Column<*>) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("REFERENCES ")
            append(column.table.render())
            append('(')
            append(column.render())
            append(')')
        }

    }

    class Unique(val conflictAlgorithm: ConflictAlgorithm) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("UNIQUE")
            if (conflictAlgorithm != ConflictAlgorithm.None) {
                append(" ON CONFLICT ")
                append(conflictAlgorithm)
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