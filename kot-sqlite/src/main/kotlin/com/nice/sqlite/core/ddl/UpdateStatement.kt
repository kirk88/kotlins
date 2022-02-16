@file:Suppress("UNUSED")

package com.nice.sqlite.core.ddl

import com.nice.sqlite.core.Dialect
import com.nice.sqlite.core.Predicate
import com.nice.sqlite.core.Table
import com.nice.sqlite.core.TableSubject
import com.nice.sqlite.core.dml.WhereClause

class UpdateStatement<T : Table>(
    val subject: TableSubject<T>,
    val conflictAlgorithm: ConflictAlgorithm,
    val values: Shell<ColumnValue>,
    val whereClause: WhereClause<T>? = null,
    val nativeBindValues: Boolean = false
) : Statement {

    override fun toString(dialect: Dialect): String = dialect.build(this)

}

class UpdateBatchStatement<T : Table>(
    val subject: TableSubject<T>,
    updateParts: Shell<UpdatePart<T>>
) : Statement {

    private val iterator = updateParts.iterator()
    private val sqlCaches = mutableMapOf<UpdatePart<*>, String>()

    private lateinit var nextSql: String
    private lateinit var nextUpdate: UpdatePart<T>

    override fun toString(dialect: Dialect): String {
        val nextStatement = UpdateStatement(
            subject,
            nextUpdate.conflictAlgorithm,
            nextUpdate.values,
            nextUpdate.whereClause,
            true
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
    @PublishedApi internal val subject: TableSubject<T>
) : Shell<UpdatePart<T>> {

    @PublishedApi
    internal val updateParts = mutableListOf<UpdatePart<T>>()

    inline fun item(buildAction: UpdatePartBuilder<T>.() -> Unit) {
        updateParts.add(UpdatePartBuilder(subject).apply(buildAction).build())
    }

    override val size: Int get() = updateParts.size
    override fun iterator(): Iterator<UpdatePart<T>> = updateParts.iterator()

}

data class UpdatePart<T : Table>(
    val conflictAlgorithm: ConflictAlgorithm,
    val values: Shell<ColumnValue>,
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
    @PublishedApi internal val subject: TableSubject<T>
) {

    @PublishedApi
    internal lateinit var values: Shell<ColumnValue>

    @PublishedApi
    internal var whereClause: WhereClause<T>? = null

    var conflictAlgorithm: ConflictAlgorithm = ConflictAlgorithm.None

    inline fun values(crossinline values: (T) -> Shell<ColumnValue>) {
        this.values = values(subject.table)
    }

    inline fun where(crossinline predicate: (T) -> Predicate) {
        this.whereClause = WhereClause(predicate(subject.table), subject)
    }

    @PublishedApi
    internal fun build(): UpdatePart<T> = UpdatePart(conflictAlgorithm, values, whereClause)

}