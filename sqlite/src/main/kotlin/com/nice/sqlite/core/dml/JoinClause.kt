@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table

class Join2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType = JoinType.INNER
) {

    inline fun on(predicate: (T, T2) -> Predicate): JoinOn2Clause<T, T2> {
        return JoinOn2Clause(
            subject,
            table2,
            type,
            predicate(subject.table, table2)
        )
    }

}

class JoinOn2Clause<T : Table, T2 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val subject: Subject<T>,
    @PublishedApi
    internal val table2: T2,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate
) {

    fun <T3 : Table> join(table3: T3): Join3Clause<T, T2, T3> {
        return Join3Clause(this, table3)
    }

    fun <T3 : Table> outerJoin(table3: T3): Join3Clause<T, T2, T3> {
        return Join3Clause(this, table3, JoinType.OUTER)
    }

    inline fun where(predicate: (T, T2) -> Predicate): Where2Clause<T, T2> {
        return Where2Clause(predicate(subject.table, table2), this)
    }

    inline fun groupBy(group: (T, T2) -> Sequence<Projection>): Group2Clause<T, T2> {
        return Group2Clause(group(subject.table, table2), this, null)
    }

    inline fun orderBy(order: (T, T2) -> Sequence<Ordering>): Order2Clause<T, T2> {
        return Order2Clause(order(subject.table, table2), this, null, null, null)
    }

    inline fun limit(limit: () -> Int): Limit2Clause<T, T2> {
        return Limit2Clause(
            limit(),
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun offset(offset: () -> Int): Offset2Clause<T, T2> {
        return Offset2Clause(
            offset(),
            limit { -1 },
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun select(selection: (T, T2) -> Sequence<Projection> = { _, _ -> emptySequence() }): Select2Statement<T, T2> {
        return Select2Statement(
            selection(
                subject.table,
                table2
            ),
            this,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

}

class Join3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType = JoinType.INNER
) {

    inline fun on(predicate: (T, T2, T3) -> Predicate): JoinOn3Clause<T, T2, T3> {
        return JoinOn3Clause(
            joinOn2Clause,
            table3,
            type,
            predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3)
        )
    }
}

class JoinOn3Clause<T : Table, T2 : Table, T3 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn2Clause: JoinOn2Clause<T, T2>,
    @PublishedApi
    internal val table3: T3,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate
) {

    fun <T4 : Table> join(table4: T4): Join4Clause<T, T2, T3, T4> {
        return Join4Clause(this, table4)
    }

    fun <T4 : Table> outerJoin(table4: T4): Join4Clause<T, T2, T3, T4> {
        return Join4Clause(this, table4, JoinType.OUTER)
    }

    inline fun where(predicate: (T, T2, T3) -> Predicate): Where3Clause<T, T2, T3> {
        return Where3Clause(
            predicate(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
            this
        )
    }

    inline fun groupBy(group: (T, T2, T3) -> Sequence<Projection>): Group3Clause<T, T2, T3> {
        return Group3Clause(
            group(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
            this,
            null
        )
    }

    inline fun orderBy(order: (T, T2, T3) -> Sequence<Ordering>): Order3Clause<T, T2, T3> {
        return Order3Clause(
            order(joinOn2Clause.subject.table, joinOn2Clause.table2, table3),
            this,
            null,
            null,
            null
        )
    }

    inline fun limit(limit: () -> Int): Limit3Clause<T, T2, T3> {
        return Limit3Clause(
            limit(),
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun offset(offset: () -> Int): Offset3Clause<T, T2, T3> {
        return Offset3Clause(
            offset(),
            limit { -1 },
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun select(selection: (T, T2, T3) -> Sequence<Projection> = { _, _, _ -> emptySequence() }): Select3Statement<T, T2, T3> {
        return Select3Statement(
            selection(
                joinOn2Clause.subject.table,
                joinOn2Clause.table2,
                table3
            ),
            this,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

}

class Join4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType = JoinType.INNER
) {

    inline fun on(predicate: (T, T2, T3, T4) -> Predicate): JoinOn4Clause<T, T2, T3, T4> {
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
}

class JoinOn4Clause<T : Table, T2 : Table, T3 : Table, T4 : Table> @PublishedApi internal constructor(
    @PublishedApi
    internal val joinOn3Clause: JoinOn3Clause<T, T2, T3>,
    @PublishedApi
    internal val table4: T4,
    @PublishedApi
    internal val type: JoinType,
    @PublishedApi
    internal val predicate: Predicate
) {

    inline fun where(predicate: (T, T2, T3, T4) -> Predicate): Where4Clause<T, T2, T3, T4> {
        return Where4Clause(
            predicate(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3,
                table4
            ),
            this
        )
    }

    inline fun groupBy(group: (T, T2, T3, T4) -> Sequence<Projection>): Group4Clause<T, T2, T3, T4> {
        return Group4Clause(
            group(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3,
                table4
            ),
            this,
            null
        )
    }

    inline fun orderBy(order: (T, T2, T3, T4) -> Sequence<Ordering>): Order4Clause<T, T2, T3, T4> {
        return Order4Clause(
            order(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3,
                table4
            ),
            this,
            null,
            null,
            null
        )
    }

    inline fun limit(limit: () -> Int): Limit4Clause<T, T2, T3, T4> {
        return Limit4Clause(
            limit(),
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun offset(offset: () -> Int): Offset4Clause<T, T2, T3, T4> {
        return Offset4Clause(
            offset(),
            limit { -1 },
            this,
            null,
            null,
            null,
            null
        )
    }

    inline fun select(selection: (T, T2, T3, T4) -> Sequence<Projection> = { _, _, _, _ -> emptySequence() }): Select4Statement<T, T2, T3, T4> {
        return Select4Statement(
            selection(
                joinOn3Clause.joinOn2Clause.subject.table,
                joinOn3Clause.joinOn2Clause.table2,
                joinOn3Clause.table3,
                table4
            ),
            this,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

}

enum class JoinType {
    INNER,
    OUTER
}