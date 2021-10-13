@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.*

class UpdateStatement<T : Table>(
    val subject: Subject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Sequence<Assignment>,
    val whereClause: WhereClause<T>? = null
) : Statement {

    override fun toString(dialect: Dialect): String {
        return dialect.build(this)
    }

}

class UpdateBatchStatement<T : Table>(
    val subject: Subject<T>,
    updateParts: Sequence<UpdatePart<T>>
) : Statement {

    private val iterator = updateParts.iterator()
    private val sqlCaches = mutableMapOf<UpdatePart<*>, String>()

    private lateinit var nextSql: String
    private lateinit var nextUpdate: UpdatePart<T>

    override fun toString(dialect: Dialect): String {
        val nextStatement =
            UpdateStatement(subject, nextUpdate.conflictAlgorithm, nextUpdate.assignments, nextUpdate.whereClause)
        return dialect.build(nextStatement)
    }

    fun next(dialect: Dialect): Executable {
        nextUpdate = iterator.next()
        nextSql = sqlCaches.getOrPut(nextUpdate) {
            toString(dialect)
        }
        return Executable(nextSql, nextUpdate.assignments)
    }

    fun hasNext(): Boolean = iterator.hasNext()

}

class UpdateBatchBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val subject: Subject<T>
) : Sequence<UpdatePart<T>> {

    @PublishedApi
    internal val updateSpecs = mutableListOf<UpdatePart<T>>()

    inline fun item(buildAction: UpdatePartBuilder<T>.() -> Unit) {
        updateSpecs.add(UpdatePartBuilder(subject).apply(buildAction).build())
    }

    override fun iterator(): Iterator<UpdatePart<T>> = updateSpecs.iterator()

}

data class UpdatePart<T : Table>(
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Assignments,
    val whereClause: WhereClause<T>?
)

class UpdatePartBuilder<T : Table>(
    @PublishedApi internal val subject: Subject<T>
) {

    private lateinit var assignments: Assignments
    private var whereClause: WhereClause<T>? = null

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    fun assignments(assignments: (T) -> Sequence<Assignment>) {
        this.assignments = Assignments(assignments(subject.table))
    }

    fun where(predicate: (T) -> Predicate) {
        this.whereClause = WhereClause(predicate(subject.table), subject)
    }

    @PublishedApi
    internal fun build(): UpdatePart<T> = UpdatePart(conflictAlgorithm, assignments, whereClause)

}