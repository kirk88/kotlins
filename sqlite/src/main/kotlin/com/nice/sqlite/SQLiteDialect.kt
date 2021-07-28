package com.nice.sqlite

import com.nice.sqlite.core.*
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*

object SQLiteDialect : Dialect {

    override fun <T : Table> build(statement: CreateTableStatement<T>): String {
        val builder = StringBuilder()

        builder.append("CREATE TABLE ")
        builder.appendTableName(statement.subject.table)
        builder.append('(')

        builder.appendSequence(statement.definitions) {
            appendDefinition(it)
        }

        builder.append(")")

        return builder.toString()
    }

    override fun <T : Table> build(statement: AlertTableStatement<T>): String {
        val builder = StringBuilder()

        builder.appendSequence(statement.definitions, delimiter = ";"){
            append("ALERT TABLE ")
            appendTableName(statement.subject.table)
            append(" ADD COLUMN ")
            appendDefinition(it)
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: DropTableStatement<T>): String {
        return "DROP TABLE \"${statement.subject.table}\""
    }

    override fun <T : Table> build(statement: SelectStatement<T>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        builder.appendProjection(statement.projections, false, "*")
        builder.append(" FROM ")
        builder.appendTableName(statement.subject.table)

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, false)
        }

        val group = statement.groupClause
        if (group != null) {
            builder.append(" GROUP BY ")
            builder.appendProjection(group.projections, false)
        }

        val having = statement.havingClause
        if (having != null) {
            builder.append(" HAVING ")
            builder.appendPredicate(having.predicate, false)
        }

        val order = statement.orderClause
        if (order != null) {
            builder.append(" ORDER BY ")
            builder.appendOrdering(order.orderings, false)
        }

        val limit = statement.limitClause
        if (limit != null) {
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        val offset = statement.offsetClause
        if (offset != null) {
            builder.append(" OFFSET ")
            builder.append(offset.offset)
        }

        return builder.toString()
    }

    override fun <T : Table, T2 : Table> build(statement: Select2Statement<T, T2>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        builder.appendProjection(statement.projections, true, "*")
        builder.append(" FROM ")
        builder.appendTableName(statement.joinOn2Clause.subject.table)

        if (statement.joinOn2Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn2Clause.table2)
        builder.append(" ON ")
        builder.appendPredicate(statement.joinOn2Clause.condition, true)

        val where = statement.where2Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, true)
        }

        val group = statement.group2Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            builder.appendProjection(group.projections, true)
        }

        val having = statement.having2Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.appendPredicate(having.predicate, true)
        }

        val order = statement.order2Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            builder.appendOrdering(order.orderings, true)
        }

        val limit = statement.limit2Clause
        if (limit != null) {
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        val offset = statement.offset2Clause
        if (offset != null) {
            builder.append(" OFFSET ")
            builder.append(offset.offset)
        }

        return builder.toString()
    }

    override fun <T : Table, T2 : Table, T3 : Table> build(statement: Select3Statement<T, T2, T3>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        builder.appendProjection(statement.projections, true, "*")
        builder.append(" FROM ")
        builder.appendTableName(statement.joinOn3Clause.joinOn2Clause.subject.table)

        if (statement.joinOn3Clause.joinOn2Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn3Clause.joinOn2Clause.table2)
        builder.append(" ON ")
        builder.appendPredicate(statement.joinOn3Clause.joinOn2Clause.condition, true)

        if (statement.joinOn3Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn3Clause.table3)
        builder.append(" ON ")
        builder.appendPredicate(statement.joinOn3Clause.condition, true)

        val where = statement.where3Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, true)
        }

        val group = statement.group3Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            builder.appendProjection(group.projections, true)
        }

        val having = statement.having3Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.appendPredicate(having.predicate, true)
        }

        val order = statement.order3Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            builder.appendOrdering(order.orderings, true)
        }

        val limit = statement.limit3Clause
        if (limit != null) {
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        val offset = statement.offset3Clause
        if (offset != null) {
            builder.append(" OFFSET ")
            builder.append(offset.offset)
        }

        return builder.toString()
    }

    override fun <T : Table, T2 : Table, T3 : Table, T4 : Table> build(statement: Select4Statement<T, T2, T3, T4>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        builder.appendProjection(statement.projections, true, "*")
        builder.append(" FROM ")
        builder.appendTableName(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table)

        if (statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.table2)
        builder.append(" ON ")
        builder.appendPredicate(
            statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.condition,
            true
        )

        if (statement.joinOn4Clause.joinOn3Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn4Clause.joinOn3Clause.table3)
        builder.append(" ON ")
        builder.appendPredicate(statement.joinOn4Clause.joinOn3Clause.condition, true)

        if (statement.joinOn4Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.appendTableName(statement.joinOn4Clause.table4)
        builder.append(" ON ")
        builder.appendPredicate(statement.joinOn4Clause.condition, true)

        val where = statement.where4Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, true)
        }

        val group = statement.group4Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            builder.appendProjection(group.projections, true)
        }

        val having = statement.having4Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.appendPredicate(having.predicate, true)
        }

        val order = statement.order4Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            builder.appendOrdering(order.orderings, true)
        }

        val limit = statement.limit4Clause
        if (limit != null) {
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        val offset = statement.offset4Clause
        if (offset != null) {
            builder.append(" OFFSET ")
            builder.append(offset.offset)
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: InsertStatement<T>): String {
        val builder = StringBuilder()
        builder.append("INSERT")
        builder.appendConflict(statement.conflict)
        builder.append("INTO ")
        builder.appendTableName(statement.subject.table)
        builder.append("(")

        builder.appendSequence(statement.assignments) {
            appendShortColumnName(it.column)
        }

        builder.append(") VALUES (")

        builder.appendSequence(statement.assignments) {
            appendValue(it.value)
        }

        builder.append(")")

        return builder.toString()
    }

    override fun <T : Table> build(statement: UpdateStatement<T>): String {
        val builder = StringBuilder()
        builder.append("UPDATE")
        builder.appendConflict(statement.conflict)
        builder.appendTableName(statement.subject.table)
        builder.append(" SET ")

        builder.appendSequence(statement.assignments) {
            appendShortColumnName(it.column)
            append(" = ")
            appendValue(it.value)
        }

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, false)
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: DeleteStatement<T>): String {
        val builder = StringBuilder()
        builder.append("DELETE FROM ")
        builder.appendTableName(statement.subject.table)

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.appendPredicate(where.predicate, false)
        }

        return builder.toString()
    }

    private fun StringBuilder.appendPredicate(value: Any?, fullFormat: Boolean = true) {
        when (value) {
            is Projection.Column -> if (fullFormat) appendFullColumnName(value)
            else appendShortColumnName(value)

            is NotExpression -> {
                append("(NOT ")
                appendPredicate(value.param, fullFormat)
                append(")")
            }

            is AndExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" AND ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            is OrExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" OR ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            is EqExpression -> {
                append('(')
                if (value.right != null) {
                    appendPredicate(value.left, fullFormat)
                    append(" = ")
                    appendPredicate(value.right, fullFormat)
                } else {
                    appendPredicate(value.left, fullFormat)
                    append(" IS NULL")
                }
                append(')')
            }

            is NeExpression -> {
                append('(')
                if (value.right != null) {
                    appendPredicate(value.left, fullFormat)
                    append(" != ")
                    appendPredicate(value.right, fullFormat)
                } else {
                    appendPredicate(value.left, fullFormat)
                    append(" IS NOT NULL")
                }
                append(')')
            }

            is LtExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" < ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            is LteExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" <= ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            is GtExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" > ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            is GteExpression -> {
                append('(')
                appendPredicate(value.left, fullFormat)
                append(" >= ")
                appendPredicate(value.right, fullFormat)
                append(')')
            }

            else -> appendValue(value)
        }
    }

    private fun StringBuilder.appendDefinition(definition: Definition) = apply {
        appendShortColumnName(definition.column)
        append(' ')
        append(definition.type)

        if (definition.defaultValue != null) {
            append(" DEFAULT ")
            appendValue(definition.defaultValue)
        }

        (definition as? SQLiteDefinition)?.meta?.apply {
            if (primaryKeyConstraint != null) {
                append(" PRIMARY KEY")
                if (primaryKeyConstraint.autoIncrement) append(" AUTOINCREMENT")
            }

            if (foreignKeyConstraint != null) {
                append(" REFERENCES ")
                appendFullColumnName(foreignKeyConstraint.references)
            }

            if (uniqueConstraint != null) {
                if (uniqueConstraint.conflict == Conflict.None) {
                    append(" UNIQUE")
                } else {
                    append(" UNIQUE ON CONFLICT ${uniqueConstraint.conflict}")
                }
            }

            if (notNullConstraint != null) {
                append(" NOT NULL")
            }

            if (updateConstraintAction != null) {
                append(" ON UPDATE $updateConstraintAction")
            }

            if (deleteConstraintAction != null) {
                append(" ON DELETE $deleteConstraintAction")
            }
        }
    }

    private fun StringBuilder.appendProjection(
        projections: Sequence<Projection>,
        fullFormat: Boolean,
        placeholder: String = ""
    ) = appendSequence(projections, placeholder = placeholder) {
        if (it is Projection.Column) {
            if (fullFormat) {
                appendFullColumnName(it)
            } else {
                appendShortColumnName(it)
            }
        } else {
            append(it)
        }
    }

    private fun StringBuilder.appendOrdering(
        orderings: Sequence<Ordering>,
        fullFormat: Boolean
    ) = appendSequence(orderings) {
        if (fullFormat) {
            appendFullColumnName(it.column)
        } else {
            appendShortColumnName(it.column)
        }
        append(' ')
        append(it.direction)
    }

    private fun <T> StringBuilder.appendSequence(
        sequence: Sequence<T>,
        delimiter: String = ", ",
        placeholder: String = "",
        action: StringBuilder.(element: T) -> Unit
    ) = apply {
        if (none()) {
            append(placeholder)
        } else {
            var count = 0
            for (element in sequence) {
                if (++count > 1) append(delimiter)
                action(element)
            }
        }
    }

    private fun StringBuilder.appendTableName(table: Table) = append("\"$table\"")

    private fun StringBuilder.appendShortColumnName(column: Projection.Column) =
        append("\"$column\"")

    private fun StringBuilder.appendFullColumnName(column: Projection.Column) =
        append("\"${column.table}\".\"$column\"")

    private fun StringBuilder.appendConflict(conflict: Conflict) = apply {
        append(' ')
        if (conflict != Conflict.None) {
            append("OR $conflict ")
        }
    }

    private fun StringBuilder.appendValue(value: Any?) = append(
        when (value) {
            null -> "NULL"
            is Number -> value
            is Boolean -> if (value) 1 else 0
            else -> value.toString().escapedSQLString()
        }
    )

    private fun String.escapedSQLString(): String = "\'${replace("'", "''")}\'"

}