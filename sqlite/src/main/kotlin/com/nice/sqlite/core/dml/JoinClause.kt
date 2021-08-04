@file:Suppress("unused")

package com.nice.sqlite.core.dml

import android.database.Cursor
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Definition
import com.nice.sqlite.core.ddl.Ordering
import com.nice.sqlite.core.ddl.StatementExecutor

sealed class JoinType {
    object Inner : JoinType() {
        override fun toString(): String = "INNER"
    }

    object Outer : JoinType() {
        override fun toString(): String = "OUTER"
    }

    object Cross : JoinType() {
        override fun toString(): String = "CROSS"
    }
}

class Join2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table> Join2Clause<T, T2>.on(predicate: (T, T2) -> Predicate): JoinOn2Clause<T, T2> {
    return JoinOn2Clause(
        subject,
        table2,
        type,
        predicate(subject.table, table2)
    )
}

inline fun <T : Table, T2 : Table> Join2Clause<T, T2>.using(using: (T, T2) -> Sequence<Definition>): JoinUsing2Clause<T, T2> {
    return JoinUsing2Clause(
        using(subject.table, table2),
        subject,
        table2,
        type
    )
}


class JoinUsing2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val definitions: Sequence<Definition>,
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table> JoinUsing2Clause<T, T2>.on(predicate: (T, T2) -> Predicate): JoinOn2Clause<T, T2> {
    return JoinOn2Clause(
        subject,
        table2,
        type,
        predicate(subject.table, table2),
        this
    )
}

class JoinOn2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinUsing2Clause: JoinUsing2Clause<T, T2>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.join(table3: T3): Join3Clause<T, T2, T3> {
    return Join3Clause(this, table3, JoinType.Inner)
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.outerJoin(table3: T3): Join3Clause<T, T2, T3> {
    return Join3Clause(this, table3, JoinType.Outer)
}


inline fun <T : Table, T2 : Table, T3 : Table> JoinOn2Clause<T, T2>.crossJoin(table3: T3): Join3Clause<T, T2, T3> {
    return Join3Clause(this, table3, JoinType.Cross)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.where(predicate: (T, T2) -> Predicate): Where2Clause<T, T2> {
    return Where2Clause(predicate(subject.table, table2), joinOn2Clause = this)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.groupBy(group: (T, T2) -> Sequence<Column<*>>): Group2Clause<T, T2> {
    return Group2Clause(group(subject.table, table2), joinOn2Clause = this)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.orderBy(order: (T, T2) -> Sequence<Ordering>): Order2Clause<T, T2> {
    return Order2Clause(order(subject.table, table2), joinOn2Clause = this)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.limit(limit: () -> Int): Limit2Clause<T, T2> {
    return Limit2Clause(
        limit(),
        joinOn2Clause = this
    )
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.offset(offset: () -> Int): Offset2Clause<T, T2> {
    return Offset2Clause(
        offset(),
        limit { -1 },
        joinOn2Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    distinct: Boolean,
    selection: (T, T2) -> Sequence<Definition>
): Select2Statement<T, T2> {
    return Select2Statement(
        selection(
            subject.table,
            table2
        ),
        joinOn2Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.select(
    executor: StatementExecutor,
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}


inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.selectDistinct(
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Select2Statement<T, T2> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table> JoinOn2Clause<T, T2>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2) -> Sequence<Definition> = { _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

class Join3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table> Join3Clause<T, T2, T3>.on(predicate: (T, T2, T3) -> Predicate): JoinOn3Clause<T, T2, T3> {
    return JoinOn3Clause(
        joinOn2Clause,
        table3,
        type,
        predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3)
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> Join3Clause<T, T2, T3>.using(using: (T, T2, T3) -> Sequence<Definition>): JoinUsing3Clause<T, T2, T3> {
    return JoinUsing3Clause(
        using(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
        joinOn2Clause,
        table3,
        type
    )
}


class JoinUsing3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val definitions: Sequence<Definition>,
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table> JoinUsing3Clause<T, T2, T3>.on(predicate: (T, T2, T3) -> Predicate): JoinOn3Clause<T, T2, T3> {
    return JoinOn3Clause(
        joinOn2Clause,
        table3,
        type,
        predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
        this
    )
}

class JoinOn3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinUsing3Clause: JoinUsing3Clause<T, T2, T3>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.join(table4: T4): Join4Clause<T, T2, T3, T4> {
    return Join4Clause(this, table4, JoinType.Inner)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.outerJoin(table4: T4): Join4Clause<T, T2, T3, T4> {
    return Join4Clause(this, table4, JoinType.Outer)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn3Clause<T, T2, T3>.crossJoin(table4: T4): Join4Clause<T, T2, T3, T4> {
    return Join4Clause(this, table4, JoinType.Cross)
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.where(predicate: (T, T2, T3) -> Predicate): Where3Clause<T, T2, T3> {
    return Where3Clause(
        predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
        joinOn3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.groupBy(group: (T, T2, T3) -> Sequence<Column<*>>): Group3Clause<T, T2, T3> {
    return Group3Clause(
        group(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
        joinOn3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.orderBy(order: (T, T2, T3) -> Sequence<Ordering>): Order3Clause<T, T2, T3> {
    return Order3Clause(
        order(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
        joinOn3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
    return Limit3Clause(
        limit(),
        joinOn3Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
    return Offset3Clause(
        offset(),
        limit { -1 },
        joinOn3Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    distinct: Boolean,
    selection: (T, T2, T3) -> Sequence<Definition>
): Select3Statement<T, T2, T3> {
    return Select3Statement(
        selection(
            joinOn2Clause.subject.table,
            joinOn2Clause.table2,
            table3
        ),
        joinOn3Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.selectDistinct(
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Select3Statement<T, T2, T3> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table> JoinOn3Clause<T, T2, T3>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2, T3) -> Sequence<Definition> = { _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}

class Join4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Join4Clause<T, T2, T3, T4>.on(predicate: (T, T2, T3, T4) -> Predicate): JoinOn4Clause<T, T2, T3, T4> {
    return JoinOn4Clause(
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
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> Join4Clause<T, T2, T3, T4>.using(using: (T, T2, T3, T4) -> Sequence<Definition>): JoinUsing4Clause<T, T2, T3, T4> {
    return JoinUsing4Clause(
        using(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        joinOn3Clause,
        table4,
        type
    )
}

class JoinUsing4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val definitions: Sequence<Definition>,
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinUsing4Clause<T, T2, T3, T4>.on(predicate: (T, T2, T3, T4) -> Predicate): JoinOn4Clause<T, T2, T3, T4> {
    return JoinOn4Clause(
        joinOn3Clause,
        table4,
        type,
        predicate(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        this
    )
}

class JoinOn4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate,
    @PublishedApi
    internal val joinUsing4Clause: JoinUsing4Clause<T, T2, T3, T4>? = null
)

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.where(
    predicate: (T, T2, T3, T4) -> Predicate
): Where4Clause<T, T2, T3, T4> {
    return Where4Clause(
        predicate(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        joinOn4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.groupBy(
    group: (T, T2, T3, T4) -> Sequence<Column<*>>
): Group4Clause<T, T2, T3, T4> {
    return Group4Clause(
        group(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        joinOn4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.orderBy(
    order: (T, T2, T3, T4) -> Sequence<Ordering>
): Order4Clause<T, T2, T3, T4> {
    return Order4Clause(
        order(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        joinOn4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
    return Limit4Clause(
        limit(),
        joinOn4Clause = this
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.offset(
    offset: () -> Int
): Offset4Clause<T, T2, T3, T4> {
    return Offset4Clause(
        offset(),
        limit { -1 },
        joinOn4Clause = this
    )
}

@PublishedApi
internal inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    distinct: Boolean,
    selection: (T, T2, T3, T4) -> Sequence<Definition>
): Select4Statement<T, T2, T3, T4> {
    return Select4Statement(
        selection(
            joinOn3Clause.joinOn2Clause.subject.table,
            joinOn3Clause.joinOn2Clause.table2,
            joinOn3Clause.table3,
            table4
        ),
        joinOn4Clause = this,
        distinct = distinct
    )
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> {
    return select(false, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.select(
    executor: StatementExecutor,
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(select(selection))
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.selectDistinct(
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Select4Statement<T, T2, T3, T4> {
    return select(true, selection)
}

inline fun <T : Table, T2 : Table, T3 : Table, T4 : Table> JoinOn4Clause<T, T2, T3, T4>.selectDistinct(
    executor: StatementExecutor,
    selection: (T, T2, T3, T4) -> Sequence<Definition> = { _, _, _, _ -> emptySequence() }
): Cursor {
    return executor.executeQuery(selectDistinct(selection))
}