@file:Suppress("UNUSED")

package com.nice.sqlite.core.dml

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.ddl.*
import com.nice.sqlite.statementExecutor

enum class JoinType {
    Inner,
    Outer,
    Cross;

    override fun toString() = when (this) {
        Inner -> "INNER JOIN"
        Outer -> "LEFT OUTER JOIN"
        Cross -> "CROSS JOIN"
    }
}

data class Join2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: TableSubject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table> Join2Clause<T, T2>.on(
    crossinline predicate: (T, T2) -> Predicate
): JoinOn2Clause<T, T2> = JoinOn2Clause(
    subject,
    table2,
    type,
    predicate(subject.table, table2)
)

inline fun <T : Table, T2 : Table> Join2Clause<T, T2>.using(
    crossinline using: (T, T2) -> Bag<Definition>
): JoinOn2Clause<T, T2> = JoinOn2Clause(
    subject,
    table2,
    type,
    using = using(subject.table, table2)
)

data class JoinOn2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: TableSubject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate? = null,
    @PublishedApi
    internal val using: Bag<Definition>? = null
)

fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.innerJoin(
    table3: T3
): Join3Clause<T, T2, T3> = Join3Clause(this, table3, JoinType.Inner)

fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.outerJoin(
    table3: T3
): Join3Clause<T, T2, T3> = Join3Clause(this, table3, JoinType.Outer)

fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.crossJoin(
    table3: T3
): Join3Clause<T, T2, T3> = Join3Clause(this, table3, JoinType.Cross)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.where(
    crossinline predicate: (T, T2) -> Predicate
): Where2Clause<T, T2> = Where2Clause(predicate(subject.table, table2), joinOn2Clause = this)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.groupBy(
    crossinline group: (T, T2) -> Bag<Column<*>>
): Group2Clause<T, T2> = Group2Clause(group(subject.table, table2), joinOn2Clause = this)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.orderBy(
    crossinline order: (T, T2) -> Bag<Ordering>
): Order2Clause<T, T2> = Order2Clause(order(subject.table, table2), joinOn2Clause = this)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.limit(
    crossinline limit: () -> Int
): Limit2Clause<T, T2> = Limit2Clause(limit(), joinOn2Clause = this)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.offset(
    crossinline offset: () -> Int
): Offset2Clause<T, T2> = Offset2Clause(offset(), limit { -1 }, joinOn2Clause = this)

@PublishedApi
internal inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition>,
    distinct: Boolean
): Select2Statement<T, T2> = Select2Statement(
    selection(
        subject.table,
        table2
    ),
    joinOn2Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, false)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.selectDistinct(
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Select2Statement<T, T2> = select(selection, true)

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2) -> Bag<Definition> = { _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

data class Join3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table> Join3Clause<T, T2, T3>.on(
    crossinline predicate: (T, T2, T3) -> Predicate
): JoinOn3Clause<T, T2, T3> = JoinOn3Clause(
    joinOn2Clause,
    table3,
    type,
    predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3)
)

inline fun <T : Table, T2 : Table, T3 : Table> Join3Clause<T, T2, T3>.using(
    crossinline using: (T, T2, T3) -> Bag<Definition>
): JoinOn3Clause<T, T2, T3> = JoinOn3Clause(
    joinOn2Clause,
    table3,
    type,
    using = using(joinOn2Clause.subject.table, joinOn2Clause.table2, table3)
)

data class JoinOn3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate? = null,
    @PublishedApi
    internal val using: Bag<Definition>? = null
)

fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.innerJoin(
    table4: T4
): Join4Clause<T, T2, T3, T4> = Join4Clause(this, table4, JoinType.Inner)

fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.outerJoin(
    table4: T4
): Join4Clause<T, T2, T3, T4> = Join4Clause(this, table4, JoinType.Outer)

fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.crossJoin(
    table4: T4
): Join4Clause<T, T2, T3, T4> = Join4Clause(this, table4, JoinType.Cross)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.where(
    crossinline predicate: (T, T2, T3) -> Predicate
): Where3Clause<T, T2, T3> = Where3Clause(
    predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
    joinOn3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.groupBy(
    crossinline group: (T, T2, T3) -> Bag<Column<*>>
): Group3Clause<T, T2, T3> = Group3Clause(
    group(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
    joinOn3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.orderBy(
    crossinline order: (T, T2, T3) -> Bag<Ordering>
): Order3Clause<T, T2, T3> = Order3Clause(
    order(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
    joinOn3Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.limit(
    crossinline limit: () -> Int
): Limit3Clause<T, T2, T3> = Limit3Clause(limit(), joinOn3Clause = this)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.offset(
    crossinline offset: () -> Int
): Offset3Clause<T, T2, T3> = Offset3Clause(offset(), limit { -1 }, joinOn3Clause = this)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition>,
    distinct: Boolean
): Select3Statement<T, T2, T3> = Select3Statement(
    selection(
        joinOn2Clause.subject.table,
        joinOn2Clause.table2,
        table3
    ),
    joinOn3Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.selectDistinct(
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Select3Statement<T, T2, T3> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3) -> Bag<Definition> = { _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))

data class Join4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Join4Clause<T, T2, T3, T4>.on(
    crossinline predicate: (T, T2, T3, T4) -> Predicate
): JoinOn4Clause<T, T2, T3, T4> = JoinOn4Clause(
    joinOn3Clause,
    table4,
    type,
    predicate(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    )
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Join4Clause<T, T2, T3, T4>.using(
    crossinline using: (T, T2, T3, T4) -> Bag<Definition>
): JoinOn4Clause<T, T2, T3, T4> = JoinOn4Clause(
    joinOn3Clause,
    table4,
    type,
    using = using(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    )
)

data class JoinOn4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate? = null,
    @PublishedApi
    internal val using: Bag<Definition>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.where(
    crossinline predicate: (T, T2, T3, T4) -> Predicate
): Where4Clause<T, T2, T3, T4> = Where4Clause(
    predicate(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    ),
    joinOn4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.groupBy(
    crossinline group: (T, T2, T3, T4) -> Bag<Column<*>>
): Group4Clause<T, T2, T3, T4> = Group4Clause(
    group(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    ),
    joinOn4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.orderBy(
    crossinline order: (T, T2, T3, T4) -> Bag<Ordering>
): Order4Clause<T, T2, T3, T4> = Order4Clause(
    order(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    ),
    joinOn4Clause = this
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.limit(
    crossinline limit: () -> Int
): Limit4Clause<T, T2, T3, T4> = Limit4Clause(limit(), joinOn4Clause = this)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.offset(
    crossinline offset: () -> Int
): Offset4Clause<T, T2, T3, T4> = Offset4Clause(offset(), limit { -1 }, joinOn4Clause = this)

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition>,
    distinct: Boolean
): Select4Statement<T, T2, T3, T4> = Select4Statement(
    selection(
        joinOn3Clause.joinOn2Clause.subject.table,
        joinOn3Clause.joinOn2Clause.table2,
        joinOn3Clause.table3,
        table4
    ),
    joinOn4Clause = this,
    distinct = distinct
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, false)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.selectDistinct(
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Select4Statement<T, T2, T3, T4> = select(selection, true)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(select(selection))

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.selectDistinct(
    database: SupportSQLiteDatabase,
    crossinline selection: (T, T2, T3, T4) -> Bag<Definition> = { _, _, _, _ -> emptyBag() }
): Cursor = database.statementExecutor.executeQuery(selectDistinct(selection))