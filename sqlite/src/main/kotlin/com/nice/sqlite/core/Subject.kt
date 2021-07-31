@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.SQLiteDefinitionBuilder
import com.nice.sqlite.core.ddl.AlertTableStatement
import com.nice.sqlite.core.ddl.Conflict
import com.nice.sqlite.core.ddl.CreateTableStatement
import com.nice.sqlite.core.ddl.DropTableStatement
import com.nice.sqlite.core.dml.*

interface Subject<T : Table> {

    val table: T

}

interface DatabaseSubject<T : Table> {

    val database: Database

}

sealed class StatementSubject<T : Table>(override val table: T) : Subject<T> {

    class Over<T : Table>(table: T) : StatementSubject<T>(table) {
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

        fun drop(action: SQLiteDefinitionBuilder.(T) -> Unit = {}): DropTableStatement<T> {
            return DropTableStatement(SQLiteDefinitionBuilder().apply {
                action(table)
            }.build(), this)
        }
    }

    class From<T : Table>(table: T) : StatementSubject<T>(table) {
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

        inline fun select(selection: (T) -> Sequence<Projection> = { emptySequence() }): SelectStatement<T> {
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

        inline fun update(
            conflict: Conflict = Conflict.None,
            values: (T) -> Sequence<Assignment>
        ): UpdateStatement<T> {
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

    class Into<T : Table>(table: T) : StatementSubject<T>(table) {
        inline fun insert(
            conflict: Conflict = Conflict.None,
            values: (T) -> Sequence<Assignment>
        ): InsertStatement<T> {
            return InsertStatement(values(table), this, conflict)
        }
    }

}

fun <T : Table> over(table: T): StatementSubject.Over<T> {
    return StatementSubject.Over(table)
}

fun <T : Table> from(table: T): StatementSubject.From<T> {
    return StatementSubject.From(table)
}

fun <T : Table> into(table: T): StatementSubject.Into<T> {
    return StatementSubject.Into(table)
}