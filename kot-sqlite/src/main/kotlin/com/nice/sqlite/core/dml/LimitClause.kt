@file:Suppress("UNUSED")

package com.nice.sqlite.core.dml

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.statementExecutor

data class LimitClause<T : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val limit: Int,
    @PublishedApi
    internal val subject: TableSubject<T>,
    @PublishedApi
    internal val whereClause: WhereClause<T>? = null,
    @PublishedApi
    internal val orderClause: OrderClause<T>? = null,
    @PublishedApi
    internal val groupClause: GroupClause<T>? = null,
    @PublishedApi
    internal val havingClause: HavingClause<T>? = null
)

inline fun <T : Table> LimitClause<T>.offset(
    crossinline offset: () -> Int
): OffsetClause<T> = OffsetClause(
    offset(),
    this,
    subject,
    whereClause = whereClause,
    orderClause = orderClause,
    groupClause = groupClause,
    havingClause = havingClause
)

@PublishedApi
internal inline fun <T : Table> LimitClause<T>.select(
    crossinline selection: (T) -> Bag<Definition>,
    distinct: Boolean
): SelectStatement<T> = SelectStatement(
    subject,
    selection(
        subject.table
    ),
    whereClause = whereClause,
    orderClause = orderClause,
    groupClause = groupClause,
    havingClause = havingClause,
    limitClause = this,
    distinct = distinct
)

inline fun <T : Table> LimitClause<T>.select(
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): SelectStatement<T> = select(selection, false)

inline fun <T : Table> LimitClause<T>.selectDistinct(
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): SelectStatement<T> = select(selection, true)

inline fun <T : Table> LimitClause<T>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table> LimitClause<T>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T) -> Bag<Definition> = { emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

data class Limit2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val limit: Int,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val where2Clause: Where2Clause<T, T2>? = null,
    @PublishedApi
    internal val order2Clause: Order2Clause<T, T2>? = null,
    @PublishedApi
    internal val group2Clause: Group2Clause<T, T2>? = null,
    @PublishedApi
    internal val having2Clause: Having2Clause<T, T2>? = null
)

inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.offset(
    crossinline offset: () -> Int
): Offset2Clause<T, T2> = Offset2Clause(
    offset(),
    joinOn2Clause = joinOn2Clause,
    where2Clause = where2Clause,
    order2Clause = order2Clause,
    group2Clause = group2Clause,
    having2Clause = having2Clause,
    limit2Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition>,
    distinct: Boolean
): Select2Statement<T, T2> = Select2Statement(
    selection(
        joinOn2Clause.subject.table,
        joinOn2Clause.table2
    ),
    joinOn2Clause = joinOn2Clause,
    where2Clause = where2Clause,
    order2Clause = order2Clause,
    group2Clause = group2Clause,
    having2Clause = having2Clause,
    limit2Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, false)

inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.selectDistinct(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, true)

inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table> Limit2Clause<T, T2>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

data class Limit3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val limit: Int,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val where3Clause: Where3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val order3Clause: Order3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val group3Clause: Group3Clause<T, T2, T3>? = null,
    @PublishedApi
    internal val having3Clause: Having3Clause<T, T2, T3>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.offset(
    crossinline offset: () -> Int
): Offset3Clause<T, T2, T3> = Offset3Clause(
    offset(),
    joinOn3Clause = joinOn3Clause,
    where3Clause = where3Clause,
    order3Clause = order3Clause,
    group3Clause = group3Clause,
    having3Clause = having3Clause,
    limit3Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition>,
    distinct: Boolean
): Select3Statement<T, T2, T3> = Select3Statement(
    selection(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3
    ),
    joinOn3Clause = joinOn3Clause,
    where3Clause = where3Clause,
    order3Clause = order3Clause,
    group3Clause = group3Clause,
    having3Clause = having3Clause,
    limit3Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.selectDistinct(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table> Limit3Clause<T, T2, T3>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

data class Limit4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val limit: Int,
    @PublishedApi
    internal val joinOn4Clause: JoinOn4Clause<T, T2, T3, T4>,
    @PublishedApi
    internal val where4Clause: Where4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val order4Clause: Order4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val group4Clause: Group4Clause<T, T2, T3, T4>? = null,
    @PublishedApi
    internal val having4Clause: Having4Clause<T, T2, T3, T4>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.offset(
    crossinline offset: () -> Int
): Offset4Clause<T, T2, T3, T4> = Offset4Clause(
    offset(),
    joinOn4Clause = joinOn4Clause,
    where4Clause = where4Clause,
    order4Clause = order4Clause,
    group4Clause = group4Clause,
    having4Clause = having4Clause,
    limit4Clause = this
)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition>,
    distinct: Boolean
): Select4Statement<T, T2, T3, T4> = Select4Statement(
    selection(
        joinOn4Clause.joinOn3Clause.joinOn2Clause.subject.table,
        joinOn4Clause.joinOn3Clause.joinOn2Clause.table2,
        joinOn4Clause.joinOn3Clause.table3,
        joinOn4Clause.table4
    ),
    joinOn4Clause = joinOn4Clause,
    where4Clause = where4Clause,
    order4Clause = order4Clause,
    group4Clause = group4Clause,
    having4Clause = having4Clause,
    limit4Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.selectDistinct(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Limit4Clause<T, T2, T3, T4>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))