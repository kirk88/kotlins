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
    val updateSpecs: Sequence<UpdateSpec<T>>
) : Statement {

    private val iterator = updateSpecs.iterator()
    private val sqlCaches = mutableMapOf<UpdateSpec<*>, String>()

    private lateinit var nextSql: String
    private lateinit var nextUpdate: UpdateSpec<T>

    override fun toString(dialect: Dialect): String {
        val nextStatement = UpdateStatement(subject, nextUpdate.conflictAlgorithm, nextUpdate.assignments, nextUpdate.whereClause)
        return dialect.build(nextStatement)
    }

    fun moveToNext(dialect: Dialect): Boolean {
        val hasNext = iterator.hasNext()
        if (hasNext) {
            nextUpdate = iterator.next()
            nextSql = sqlCaches.getOrPut(nextUpdate) {
                toString(dialect)
            }
        }
        return hasNext
    }

    fun next(): Executable = Executable(nextSql, nextUpdate.assignments)

}

class UpdateBatchBuilder<T : Table> @PublishedApi internal constructor(
    @PublishedApi internal val subject: Subject<T>
) : Sequence<UpdateSpec<T>> {

    @PublishedApi
    internal val updateSpecs = mutableListOf<UpdateSpec<T>>()

    inline fun item(buildAction: UpdateSpecBuilder<T>.() -> Unit) {
        updateSpecs.add(UpdateSpecBuilder(subject).apply(buildAction).build())
    }

    override fun iterator(): Iterator<UpdateSpec<T>> = updateSpecs.iterator()

}

data class UpdateSpec<T : Table>(
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Assignments,
    val whereClause: WhereClause<T>?
)

class UpdateSpecBuilder<T : Table>(
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
    internal fun build(): UpdateSpec<T> = UpdateSpec(conflictAlgorithm, assignments, whereClause)

}