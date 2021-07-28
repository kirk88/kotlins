@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.SQLiteDefinitionBuilder
import com.nice.sqlite.core.ddl.AlertTableStatement
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.CreateTableStatement
import com.nice.sqlite.core.ddl.DropTableStatement
import com.nice.sqlite.core.dml.*

open class Subject<T : Table>(val table: T) {

    override fun toString(): String {
        return table.toString()
    }

    class Over<T : Table>(table: T) : Subject<T>(table) {
        inline fun create(action: SQLiteDefinitionBuilder.(T) -> Unit): CreateTableStatement<T> {
            return CreateTableStatement(SQLiteDefinitionBuilder().apply {
                action(table)
            }.build(), this)
        }

        inline fun alert(action: SQLiteDefinitionBuilder.(T) -> Unit): AlertTableStatement<T> {
            return AlertTableStatement(SQLiteDefinitionBuilder().apply {
                action(table)
            }.build(), this)
        }

        fun drop(): DropTableStatement<T> {
            return DropTableStatement(this)
        }
    }

    class From<T : Table>(table: T) : Subject<T>(table) {
        fun <T2 : Table> join(table2: T2): Join2Clause<T, T2> {
            return Join2Clause(this, table2)
        }

        fun <T2 : Table> outerJoin(table2: T2): Join2Clause<T, T2> {
            return Join2Clause(this, table2, JoinType.OUTER)
        }

        inline fun where(predicate: (T) -> Predicate): WhereClause<T> {
            return WhereClause(predicate(table), this)
        }

        inline fun groupBy(group: (T) -> Sequence<Projection>): GroupClause<T> {
            return GroupClause(group(table), this, null)
        }

        inline fun orderBy(order: (T) -> Sequence<Ordering>): OrderClause<T> {
            return OrderClause(order(table), this, null, null, null)
        }

        inline fun limit(limit: () -> Int): LimitClause<T> {
            return LimitClause(
                limit(),
                this,
                null,
                null,
                null,
                null
            )
        }

        inline fun offset(offset: () -> Int): OffsetClause<T> {
            return OffsetClause(
                offset(),
                limit { -1 },
                this,
                null,
                null,
                null,
                null
            )
        }

        inline fun select(selection: (T) -> Sequence<Projection>): SelectStatement<T> {
            return SelectStatement(
                selection(table),
                this,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        fun select(): SelectStatement<T> {
            return SelectStatement(
                emptySequence(),
                this,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        inline fun update(conflict: Conflict = Conflict.None, values: (T) -> Sequence<Assignment>): UpdateStatement<T> {
            return UpdateStatement(
                values(table),
                this,
                null,
                conflict
            )
        }

        fun delete(): DeleteStatement<T> {
            return DeleteStatement(
                this,
                null
            )
        }
    }

    class Into<T : Table>(table: T) : Subject<T>(table) {
        inline fun insert(conflict: Conflict = Conflict.None, values: (T) -> Sequence<Assignment>): InsertStatement<T> {
            return InsertStatement(values(table), this, conflict)
        }
    }
}

fun <T : Table> over(table: T): Subject.Over<T> {
    return Subject.Over(table)
}

fun <T : Table> from(table: T): Subject.From<T> {
    return Subject.From(table)
}

fun <T : Table> into(table: T): Subject.Into<T> {
    return Subject.Into(table)
}