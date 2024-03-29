package com.nice.sqlite

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ViewType
import com.nice.sqlite.core.ddl.*

object SQLiteDialect : Dialect {

    override fun <T : Table> build(statement: TableCreateStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<Column<*>>()
        if (!columns.isEmpty()) {
            builder.append("CREATE TABLE IF NOT EXISTS ")
            builder.append(statement.subject.table.render())
            builder.append(" (")

            columns.joinTo(builder, postfix = ")") {
                compileColumnSql(it)
            }
        }

        val indexes = statement.definitions.filterIsInstance<Index>()
        if (!indexes.isEmpty()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                compileCreateIndexSql(it)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: TableAlterStatement<T>): String {
        val builder = StringBuilder()

        val columns = statement.definitions.filterIsInstance<Column<*>>()
        if (!columns.isEmpty()) {
            columns.joinTo(builder, separator = ";") {
                "ALTER TABLE ${statement.subject.table.render()} ADD ${compileColumnSql(it)}"
            }
        }

        val indexes = statement.definitions.filterIsInstance<Index>()
        if (!indexes.isEmpty()) {
            indexes.joinTo(builder, separator = ";", prefix = ";") {
                compileCreateIndexSql(it)
            }
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: TableDropStatement<T>): String {
        val builder = StringBuilder()

        if (statement.definitions.isEmpty()) {
            builder.append("DROP TABLE ")
            builder.append(statement.subject.table.render())
        } else {
            check(statement.definitions.none { it is Column<*> }) { "Drop columns are not supported yet" }

            val indexes = statement.definitions.map { it as Index }
            indexes.joinTo(builder, separator = ";") {
                "DROP INDEX IF EXISTS ${it.render()}"
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
        if (statement.definitions.isEmpty()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.render()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.subject.table.render())

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render(this))
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
            builder.append(having.predicate.render(this))
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
        if (statement.definitions.isEmpty()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn2Clause.subject.table.render())
        builder.append(' ')
        builder.append(statement.joinOn2Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn2Clause.table2.render())

        statement.joinOn2Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn2Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }

        statement.where2Clause?.let { where ->
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender(this))
        }

        statement.group2Clause?.let { group ->
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        statement.having2Clause?.let { having ->
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender(this))
        }

        statement.order2Clause?.let { order ->
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.fullRender()
            }
        }

        statement.limit2Clause?.let { limit ->
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        statement.offset2Clause?.let { offset ->
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
        if (statement.definitions.isEmpty()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn3Clause.joinOn2Clause.subject.table.render())
        builder.append(' ')
        builder.append(statement.joinOn3Clause.joinOn2Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn3Clause.joinOn2Clause.table2.render())

        statement.joinOn3Clause.joinOn2Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn3Clause.joinOn2Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }
        builder.append(' ')
        builder.append(statement.joinOn3Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn3Clause.table3.render())

        statement.joinOn3Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn3Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }

        statement.where3Clause?.let { where ->
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender(this))
        }

        statement.group3Clause?.let { group ->
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        val having = statement.having3Clause
        if (having != null) {
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender(this))
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
        if (statement.definitions.isEmpty()) {
            builder.append('*')
        } else {
            statement.definitions.joinTo(builder) {
                it.fullRender()
            }
        }
        builder.append(" FROM ")
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table.render())
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.table2.render())

        statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn4Clause.joinOn3Clause.joinOn2Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn4Clause.joinOn3Clause.table3.render())

        statement.joinOn4Clause.joinOn3Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn4Clause.joinOn3Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }
        builder.append(' ')
        builder.append(statement.joinOn4Clause.type)
        builder.append(' ')
        builder.append(statement.joinOn4Clause.table4.render())

        statement.joinOn4Clause.using?.joinTo(
            builder,
            prefix = " USING (",
            postfix = ")"
        ) {
            it.render()
        }

        statement.joinOn4Clause.predicate?.let { on ->
            builder.append(" ON ")
            builder.append(on.fullRender(this))
        }

        statement.where4Clause?.let { where ->
            builder.append(" WHERE ")
            builder.append(where.predicate.fullRender(this))
        }

        statement.group4Clause?.let { group ->
            builder.append(" GROUP BY ")
            group.columns.joinTo(builder) {
                it.fullRender()
            }
        }

        statement.having4Clause?.let { having ->
            builder.append(" HAVING ")
            builder.append(having.predicate.fullRender(this))
        }

        statement.order4Clause?.let { order ->
            builder.append(" ORDER BY ")
            order.orderings.joinTo(builder) {
                it.fullRender()
            }
        }

        statement.limit4Clause?.let { limit ->
            builder.append(" LIMIT ")
            builder.append(limit.limit)
        }

        statement.offset4Clause?.let { offset ->
            builder.append(" OFFSET ")
            builder.append(offset.offset)
        }

        return builder.toString()
    }

    override fun <T : Table> build(statement: InsertStatement<T>): String {
        val builder = StringBuilder()

        builder.append(compileInsertSql(statement.subject.table, statement.conflictAlgorithm))

        builder.append(" (")

        statement.values.joinTo(builder) {
            it.column.render()
        }

        builder.append(") VALUES (")

        if (statement.nativeBindValues) {
            statement.values.joinTo(builder) {
                if (it.value is Definition) it.value.render() else "?"
            }
        } else {
            statement.values.joinTo(builder) {
                it.value.toSqlString()
            }
        }

        builder.append(')')

        return builder.toString()
    }

    override fun <T : Table> build(statement: UpdateStatement<T>): String {
        val builder = StringBuilder()
        builder.append("UPDATE ")
        if (statement.conflictAlgorithm != ConflictAlgorithm.None) {
            builder.append("OR ")
            builder.append(statement.conflictAlgorithm)
            builder.append(' ')
        }
        builder.append(statement.subject.table.render())
        builder.append(" SET ")

        if (statement.nativeBindValues) {
            statement.values.joinTo(builder) {
                "${it.column.render()} = ${if (it.value is Definition) it.value.render() else "?"}"
            }
        } else {
            statement.values.joinTo(builder) {
                "${it.column.render()} = ${it.value.toSqlString()}"
            }
        }

        val where = statement.whereClause
        if (where != null) {
            builder.append(" WHERE ")
            builder.append(where.predicate.render(this))
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
            builder.append(where.predicate.render(this))
        }

        return builder.toString()
    }

    override fun build(statement: UnionStatement): String {
        val builder = StringBuilder()

        builder.append(statement.statement1.toString(this))
        builder.append("UNION")
        if (statement.all) {
            builder.append(" ALL")
        }
        builder.append(statement.statement2.toString(this))

        return builder.toString()
    }

    override fun build(statement: ViewCreateStatement): String {
        val builder = StringBuilder()

        builder.append("CREATE ")
        val type = statement.subject.view.type
        if (type != ViewType.None) {
            builder.append(type)
            builder.append(' ')
        }
        builder.append("VIEW IF NOT EXISTS ")
        builder.append(statement.subject.view.render())
        builder.append(' ')
        builder.append("AS ")
        builder.append(statement.subject.view.statement.toString(this))

        return builder.toString()
    }

    override fun build(statement: ViewSelectStatement): String {
        return "SELECT * FROM ${statement.subject.view.render()}"
    }

    override fun <T : Table> build(statement: TriggerCreateStatement<T>): String {
        val builder = StringBuilder()
        builder.append("CREATE TRIGGER IF NOT EXISTS ")
        builder.append(statement.subject.trigger.render())
        builder.append(' ')
        builder.append(statement.subject.trigger.event.render())
        builder.append(' ')
        builder.append(statement.subject.trigger.where.render())
        builder.append(' ')

        val predicate = statement.subject.trigger.predicate
        if (predicate != null) {
            builder.append("WHEN ")
            builder.append(predicate.render(this))
            builder.append(' ')
        }

        builder.append("BEGIN")
        builder.append(' ')
        builder.append(statement.subject.trigger.statement.toString(this))
        builder.append("; END")
        return builder.toString()
    }

    private fun compileColumnSql(column: Column<*>): String = buildString {
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

            if (uniqueConstraint != null) {
                append(' ')
                append(uniqueConstraint)
            }

            if (notNullConstraint != null) {
                append(' ')
                append(notNullConstraint)
            }

            if (referencesConstraint != null) {
                append(' ')
                append(referencesConstraint)
            }

            if (onUpdateConstraint != null) {
                append(' ')
                append(onUpdateConstraint)
            }

            if (onDeleteConstraint != null) {
                append(' ')
                append(onDeleteConstraint)
            }
        }
    }

    private fun compileCreateIndexSql(index: Index): String = buildString {
        append("CREATE ")
        if (index.unique) {
            append("UNIQUE ")
        }
        append("INDEX IF NOT EXISTS ")
        append(index.fullRender())
    }

    private fun compileInsertSql(table: Table, conflictAlgorithm: ConflictAlgorithm): String = buildString {
        append("INSERT")
        if (conflictAlgorithm != ConflictAlgorithm.None) {
            append(" OR ")
            append(conflictAlgorithm)
        }
        append(" INTO ")
        append(table.render())
    }

}