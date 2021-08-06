package com.nice.sqlite

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.core.dml.*

object SQLiteDialect : Dialect {

    override fun <T : Table> build(statement: CreateStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<Column<*>>()
        if (!columns.none()) {
            builder.append("CREATE TABLE IF NOT EXISTS ")
            builder.append(statement.subject.table.renderedName)
            builder.append(" (")

            columns.joinTo(builder, postfix = ")") {
                decompileColumnSql(it)
            }
        }

        val indexes = statement.definitions.filterIsInstance<Index>()
        if (!indexes.none()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                decompileCreateIndexSql(it)
            }
        }
        return builder.toString()
    }

    override fun <T : Table> build(statement: AlterStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<Column<*>>()
        if (!columns.none()) {
            columns.joinTo(builder, separator = ";") {
                "ALTER TABLE ${statement.subject.table.renderedName} ADD COLUMN ${
                    decompileColumnSql(
                        it
                    )
                }"
            }
        }

        val indexes = statement.definitions.filterIsInstance<Index>()
        if (!indexes.none()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                decompileCreateIndexSql(it)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: DropStatement<T>): String {
        val builder = StringBuilder()

        if (statement.definitions.none()) {
            builder.append("DROP TABLE ")
            builder.append(statement.subject.table.renderedName)
        } else {
            check(statement.definitions.none {
                it is Column<*>
            }) { "Drop columns are not supported yet" }

            val indexes = statement.definitions.map { it as Index }
            indexes.joinTo(builder, separator = ";") {
                decompileDropIndexSql(it)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: SelectStatement<T>): String {
        val builder = StringBuilder()

        builder.append("SELECT ")
        if (statement.distinct) {
            builder.append("DISTINCT ")
        }
        if (statement.definitions.none()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.render()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.subject.table.renderedName)

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render())
        }

        val group = statement.groupClause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
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
        if (statement.distinct) {
            builder.append("DISTINCT ")
        }
        if (statement.definitions.none()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn2Clause.subject.table.renderedName)
        builder.append(' ')
        builder.append(statement.joinOn2Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn2Clause.table2.renderedName)

        statement.joinOn2Clause.joinUsing2Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn2Clause.predicate.fullRender())

        val where = statement.where2Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender())
        }

        val group = statement.group2Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        val having = statement.having2Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender())
        }

        val order = statement.order2Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.fullRender()
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
        if (statement.distinct) {
            builder.append("DISTINCT ")
        }
        if (statement.definitions.none()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.subject.table.renderedName)
        builder.append(' ')
        builder.append(statement.joinOn3Clause.joinOn2Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.table2.renderedName)

        statement.joinOn3Clause.joinOn2Clause.joinUsing2Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.predicate.fullRender())
        builder.append(' ')
        builder.append(statement.joinOn3Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn3Clause.table3.renderedName)

        statement.joinOn3Clause.joinUsing3Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn3Clause.predicate.fullRender())

        val where = statement.where3Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender())
        }

        val group = statement.group3Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        val having = statement.having3Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender())
        }

        val order = statement.order3Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.fullRender()
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
        if (statement.distinct) {
            builder.append("DISTINCT ")
        }
        if (statement.definitions.none()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table.renderedName)
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.table2.renderedName)

        statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.joinUsing2Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.predicate.fullRender())
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.table3.renderedName)

        statement.joinOn4Clause.joinOn3Clause.joinUsing3Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.predicate.fullRender())
        builder.append(' ')
        builder.append(statement.joinOn4Clause.type)
        builder.append(" JOIN ")
        builder.append(statement.joinOn4Clause.table4.renderedName)

        statement.joinOn4Clause.joinUsing4Clause?.definitions?.joinTo(builder, prefix = " USING (", postfix = ")") {
            it.fullRender()
        }

        builder.append(" ON ")
        builder.append(statement.joinOn4Clause.predicate.fullRender())

        val where = statement.where4Clause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender())
        }

        val group = statement.group4Clause
        if (group != null) {
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        val having = statement.having4Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender())
        }

        val order = statement.order4Clause
        if (order != null) {
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.fullRender()
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

        builder.append(decompileInsertSql(statement.subject.table, statement.conflict))

        builder.append(" (")

        statement.assignments.joinTo(builder) {
            it.column.render()
        }

        builder.append(") VALUES (")

        statement.assignments.joinTo(builder) {
            it.value.toSqlString()
        }

        builder.append(')')

        return builder.toString()
    }

    override fun <T : Table> build(statement: BatchInsertStatement<T>): String {
        val assignments = statement.assignments

        val builder = StringBuilder()

        builder.append(decompileInsertSql(statement.subject.table, statement.conflict))

        builder.append(" (")

        assignments.joinTo(builder) {
            it.column.render()
        }

        builder.append(") VALUES (")

        assignments.joinTo(builder) {
            "?"
        }

        builder.append(')')

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
        builder.append(statement.subject.table.renderedName)
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
        builder.append(statement.subject.table.renderedName)

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render())
        }

        return builder.toString()
    }

    private fun decompileColumnSql(column: Column<*>): String = buildString {
        append(column.render())
        append(' ')
        append(column.type)

        with(column.meta) {
            if (defaultConstraint != null) {
                append(' ')
                append(defaultConstraint)
            }

            if (primaryKeyConstraint != null) {
                append(' ')
                append(primaryKeyConstraint)
            }

            if (foreignKeyConstraint != null) {
                append(' ')
                append(foreignKeyConstraint)
            }

            if (uniqueConstraint != null) {
                append(' ')
                append(uniqueConstraint)
            }

            if (notNullConstraint != null) {
                append(' ')
                append(notNullConstraint)
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

    private fun decompileCreateIndexSql(index: Index): String = buildString {
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

        append(' ')
        append(index.fullRender())
    }

    private fun decompileDropIndexSql(index: Index): String = buildString {
        with(index.meta) {
            append("DROP INDEX")

            if (ifExists != null) {
                append(' ')
                append(ifExists)
            }
        }

        append(' ')
        append(index.render())
    }

    private fun decompileInsertSql(table: Table, conflict: Conflict): String = buildString {
        append("INSERT ")
        if (conflict != Conflict.None) {
            append("OR ")
            append(conflict)
            append(' ')
        }
        append("INTO ")
        append(table.renderedName)
    }

}