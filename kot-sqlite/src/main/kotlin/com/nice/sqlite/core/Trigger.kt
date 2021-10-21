@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.surrounding

class Trigger<T : Table> private constructor(
    val name: String,
    val event: TriggerEvent,
    val where: TriggerWhere<T>,
    val predicate: Predicate?,
    val statement: Statement
) : Renderer {

    override fun render(): String = name.surrounding()

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Trigger<*>

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    class Builder<T : Table>(private val name: String) {

        private var event: TriggerEvent? = null
        private var where: TriggerWhere<T>? = null
        private var predicate: Predicate? = null
        private var statement: Statement? = null

        fun at(time: TriggerTime, type: TriggerType) = apply {
            this.event = TriggerEvent(time, type)
        }

        fun on(table: T, column: (T) -> Column<*>? = { null }) = apply {
            this.where = TriggerWhere(table, column(table))
        }

        fun whence(predicate: (T) -> Predicate) = apply {
            this.predicate = predicate(requireNotNull(where) {
                "The whence(predicate) method must be called after the on(table, column) method  "
            }.table)
        }

        fun trigger(statement: () -> Statement) = apply {
            this.statement = statement()
        }

        fun build(): Trigger<T> {
            val event = requireNotNull(this.event) {
                "You must call the event(event) method"
            }
            val where = requireNotNull(this.where) {
                "You must call the on(table, column) method"
            }
            val statement = requireNotNull(this.statement) {
                "You must call the action(statement) method"
            }
            return Trigger(name, event, where, predicate, statement)
        }

    }

}

enum class TriggerTime {
    Before,
    After;

    override fun toString(): String = name.uppercase()
}

enum class TriggerType {
    Insert,
    Update,
    Delete;

    override fun toString(): String = name.uppercase()
}

data class TriggerEvent(
    val time: TriggerTime,
    val type: TriggerType
) : Renderer {
    override fun render(): String = "$time $type"
}

data class TriggerWhere<T : Table>(
    val table: T,
    val column: Column<*>?
) : Renderer {
    override fun render(): String = buildString {
        if (column != null) {
            append("OF ")
            append(column.render())
            append(' ')
        }
        append("ON ")
        append(table.render())
    }
}