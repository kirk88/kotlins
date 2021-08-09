@file:Suppress("unused")

package com.nice.sqlite.core.ddl

enum class ColumnConstraintAction(
    private val value: String
) {
    SetNull("SET NULL"),
    SetDefault("SET DEFAULT"),
    SetRestrict("SET DEFAULT"),
    Cascade("CASCADE"),
    NoAction("NO ACTION");

    override fun toString(): String {
        return value
    }
}

sealed class ColumnConstraint {

    class Default(val value: Any) : ColumnConstraint() {
        override fun toString(): String = buildString {
            append("DEFAULT ")
            append(value.toSqlString())
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

    class ForeignKey(val references: Column) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("REFERENCES ")
            append(references.fullRender())
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