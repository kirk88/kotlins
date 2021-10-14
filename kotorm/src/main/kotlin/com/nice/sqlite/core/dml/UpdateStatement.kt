@file:Suppress("unused")

package com.nice.sqlite.core.dml

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Subject
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.ddl.ConflictAlgorithm
import com.nice.sqlite.core.ddl.Executable
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.Value

class UpdateStatement<T : Table>(
    val subject: Subject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val assignments: Sequence<Value>,
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
            UpdateStatement(
                subject,
                nextUpdate.conflictAlgorithm,
                nextUpdate.values,
                nextUpdate.whereClause
            )
        return dialect.build(nextStatement)
    }

    fun next(dialect: Dialect): Executable {
        nextUpdate = iterator.next()
        nextSql = sqlCaches.getOrPut(nextUpdate) {
            toString(dialect)
        }
        return Executable(nextSql, nextUpdate.values)
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
    val values: Sequence<Value>,
    val whereClause: WhereClause<T>?
) {
    private val id: Int = values.joinToString(prefix = "$conflictAlgorithm, ") {
        it.column.name
    }.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdatePart<*>

        if (id != other.id) return false
        if (whereClause != other.whereClause) return false

        return true
    }

    override fun hashCode(): Int {
        var result = whereClause?.hashCode() ?: 0
        result = 31 * result + id
        return result
    }

}

class UpdatePartBuilder<T : Table>(
    @PublishedApi internal val subject: Subject<T>
) {

    private lateinit var values: Sequence<Value>
    private var whereClause: WhereClause<T>? = null

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    fun values(values: (T) -> Sequence<Value>) {
        this.values = values.invoke(subject.table)
    }

    fun where(predicate: (T) -> Predicate) {
        this.whereClause = WhereClause(predicate(subject.table), subject)
    }

    @PublishedApi
    internal fun build(): UpdatePart<T> = UpdatePart(conflictAlgorithm, values, whereClause)

}