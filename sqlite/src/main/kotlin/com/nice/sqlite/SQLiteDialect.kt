package com.nice.sqlite

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.AlertTableStatement
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.CreateTableStatement
import com.nice.sqlite.core.ddl.DropTableStatement
import com.nice.sqlite.core.dml.*
import com.nice.sqlite.core.escapedSQLString

object SQLiteDialect : Dialect {

    override fun <T : Table> build(statement: CreateTableStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<SQLiteColumn>()
        if (!columns.none()) {
            builder.append("CREATE TABLE")
            builder.append(statement.subject.table.render())
            builder.append(" (")

            columns.joinTo(builder, postfix = ")") {
                compileColumnSql(it)
            }
        }

        val indexes = statement.definitions.filterIsInstance<SQLiteIndex>()
        if (!indexes.none()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                compileCreateIndexSql(it, statement.subject.table)
            }
        }
        return builder.toString()
    }

    override fun <T : Table> build(statement: AlertTableStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<SQLiteColumn>()
        if (!columns.none()) {
            columns.joinTo(builder, ";") {
                "ALERT TABLE ${statement.subject.table.render()} ADD COLUMN ${compileColumnSql(it)}"
            }
        }

        val indexes = statement.definitions.filterIsInstance<SQLiteIndex>()
        if (!indexes.none()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                compileCreateIndexSql(it, statement.subject.table)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: DropTableStatement<T>): String {
        val builder = StringBuilder()

        if (statement.definitions.none()) {
            builder.append("DROP TABLE ")
            builder.append(statement.subject.table.render())
        } else {
            check(statement.definitions.none {
                it is SQLiteColumn
            }) { "Drop columns are not supported yet" }

            val indexes = statement.definitions.map { it as SQLiteIndex }
            indexes.joinTo(builder, separator = ";") {
                compileDropIndexSql(it, statement.subject.table)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: SelectStatement<T>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        if (statement.projections.none()) {
            builder.append('*')
        } else {
            statement.projections.joinTo(builder) {
                it.render()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.subject.table.render())

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render())
        }

        val group = statement.groupClause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.projections.joinTo(builder) {
                it.render()
            }
        }

        val having = statement.havingClause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.render())
        }

        val order = statement.orderClause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.render()
            }
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
        if (statement.projections.none()) {
            builder.append('*')
        } else {
            statement.projections.joinTo(builder) {
                it.render(true)
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn2Clause.subject.table.render())

        if (statement.joinOn2Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.append(statement.joinOn2Clause.table2.render())
        builder.append(" ON ")
        builder.append(statement.joinOn2Clause.predicate.render(true))

        val where = statement.where2Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render(true))
        }

        val group = statement.group2Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.projections.joinTo(builder) {
                it.render(true)
            }
        }

        val having = statement.having2Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.render(true))
        }

        val order = statement.order2Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.render(true)
            }
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
        if (statement.projections.none()) {
            builder.append('*')
        } else {
            statement.projections.joinTo(builder) {
                it.render(true)
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.subject.table.render())

        if (statement.joinOn3Clause.joinOn2Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.table2.render())
        builder.append(" ON ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.predicate.render(true))

        if (statement.joinOn3Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.append(statement.joinOn3Clause.table3.render())
        builder.append(" ON ")
        builder.append(statement.joinOn3Clause.predicate.render(true))

        val where = statement.where3Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render(true))
        }

        val group = statement.group3Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.projections.joinTo(builder) {
                it.render(true)
            }
        }

        val having = statement.having3Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.render(true))
        }

        val order = statement.order3Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.render(true)
            }
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
        if (statement.projections.none()) {
            builder.append('*')
        } else {
            statement.projections.joinTo(builder) {
                it.render(true)
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table.render())

        if (statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.type == JoinType.OUTER) builder.append(
            " OUTER"
        )
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.table2.render())
        builder.append(" ON ")
        builder.append(
            statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.predicate.render(true)
        )

        if (statement.joinOn4Clause.joinOn3Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.table3.render())
        builder.append(" ON ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.predicate.render(true))

        if (statement.joinOn4Clause.type == JoinType.OUTER) builder.append(" OUTER")
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.table4.render())
        builder.append(" ON ")
        builder.append(statement.joinOn4Clause.predicate.render(true))

        val where = statement.where4Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render(true))
        }

        val group = statement.group4Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.projections.joinTo(builder) {
                it.render(true)
            }
        }

        val having = statement.having4Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.render(true))
        }

        val order = statement.order4Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.render(true)
            }
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
        builder.append("INSERT ")
        if (statement.conflict != Conflict.None) {
            builder.append("OR ")
            builder.append(statement.conflict)
            builder.append(' ')
        }
        builder.append("INTO ")
        builder.append(statement.subject.table.render())
        builder.append(" (")

        statement.assignments.joinTo(builder) {
            it.column.render()
        }

        builder.append(") VALUES (")

        statement.assignments.joinTo(builder) {
            it.value.escapedSQLString()
        }

        builder.append(")")

        return builder.toString()
    }

    override fun <T : Table> build(statement: UpdateStatement<T>): String {
        val builder = StringBuilder()
        builder.append("UPDATE ")
        if (statement.conflict != Conflict.None) {
            builder.append("OR ")
            builder.append(statement.conflict)
            builder.append(' ')
        }
        builder.append(statement.subject.table.render())
        builder.append(" SET ")

        statement.assignments.joinTo(builder) {
            it.render()
        }

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render())
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: DeleteStatement<T>): String {
        val builder = StringBuilder()
        builder.append("DELETE FROM ")
        builder.append(statement.subject.table.render())

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render())
        }

        return builder.toString()
    }

    private fun compileColumnSql(column: SQLiteColumn): String = buildString {
        append(column.render())

        with(column.meta) {
            if (primaryKey != null) {
                append(' ')
                append(primaryKey)
            }

            if (foreignKey != null) {
                append(' ')
                append(foreignKey)
            }

            if (unique != null) {
                append(' ')
                append(unique)
            }

            if (notNull != null) {
                append(' ')
                append(notNull)
            }

            if (onUpdateAction != null) {
                append(" ON UPDATE ")
                append(onUpdateAction)
            }

            if (onDeleteAction != null) {
                append(" ON DELETE ")
                append(onDeleteAction)
            }
        }
    }

    private fun compileCreateIndexSql(index: SQLiteIndex, table: Table): String = buildString {
        with(index.meta) {
            append("CREATE")

            if (unique != null) {
                append(' ')
                append(unique)
            }

            append(" INDEX")

            if (ifNotExists != null) {
                append(' ')
                append(ifNotExists)
            }
        }

        append(" \"${index.name}\"")
        append(" ON ")
        append(table.render())
        append(' ')
        append(index.render())
    }

    private fun compileDropIndexSql(index: SQLiteIndex, table: Table): String = buildString {
        with(index.meta) {
            append("DROP INDEX")

            if (ifExists != null) {
                append(' ')
                append(ifExists)
            }
        }

        append(" \"${index.name}\"")
        append(" ON ")
        append(table.render())
    }

}