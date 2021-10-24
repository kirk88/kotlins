@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

sealed class ConstraintAction(
    private val name: String
) {
    object SetNull : ConstraintAction("SET NULL")
    object SetDefault : ConstraintAction("SET DEFAULT")
    object SetRestrict : ConstraintAction("SET RESTRICT")
    object Cascade : ConstraintAction("CASCADE")
    object NoAction : ConstraintAction("NO ACTION")

    override fun toString(): String = name
}

sealed class ColumnConstraint {

    class Default<V : Any> : ColumnConstraint {
        private val value: Any

        constructor(value: V) : super() {
            this.value = value
        }

        constructor(value: Defined) : super() {
            this.value = value
        }

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

    class References(val column: Column<*>) : ColumnConstraint() {

        override fun toString(): String = buildString {
            append("REFERENCES ")
            append(column.table.render())
            append('(')
            append(column.render())
            append(')')
        }

    }

    class OnUpdate(val action: ConstraintAction) : ColumnConstraint() {
        override fun toString(): String = buildString {
            append("ON UPDATE ")
            append(action)
        }
    }

    class OnDelete(val action: ConstraintAction) : ColumnConstraint() {
        override fun toString(): String = buildString {
            append("ON DELETE ")
            append(action)
        }
    }

}

fun <V : Any> Default(value: V) = ColumnConstraint.Default(value)
fun <V : Any> Default(value: Defined) = ColumnConstraint.Default<V>(value)
fun PrimaryKey(autoIncrement: Boolean = false) = ColumnConstraint.PrimaryKey(autoIncrement)
fun References(column: Column<*>) = ColumnConstraint.References(column)
fun Unique(conflictAlgorithm: ConflictAlgorithm) = ColumnConstraint.Unique(conflictAlgorithm)
fun NotNull() = ColumnConstraint.NotNull
fun OnUpdate(action: ConstraintAction) = ColumnConstraint.OnUpdate(action)
fun OnDelete(action: ConstraintAction) = ColumnConstraint.OnDelete(action)