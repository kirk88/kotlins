@file:Suppress("unused")

package com.nice.sqlite.core

import com.nice.sqlite.core.ddl.Column
import com.nice.sqlite.core.ddl.Renderer
import com.nice.sqlite.core.ddl.Statement
import com.nice.sqlite.core.ddl.surrounding

class Trigger private constructor(
    val name: String,
    val event: TriggerEvent,
    val where: TriggerWhere,
    val predicate: Predicate?,
    val statement: Statement
) : Renderer {

    override fun render(): String = name.surrounding()

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Trigger

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    class Builder(private val name: String) {

        private var event: TriggerEvent? = null
        private var where: TriggerWhere? = null
        private var predicate: Predicate? = null
        private var statement: Statement? = null

        fun event(time: TriggerTime, type: TriggerType) = apply {
            this.event = TriggerEvent(time, type)
        }

        fun <T : Table> on(table: T, column: (T) -> Column<*>? = { null }) = apply {
            this.where = TriggerWhere(table, column(table))
        }

        fun whence(predicate: () -> Predicate) = apply {
            this.predicate = predicate()
        }

        fun action(statement: () -> Statement) = apply {
            this.statement = statement(requireNotNull(where) {
                "The action(statement) method must be called after the on(table, column) method"
            }.table)
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
    override fun render(): String {
        TODO("Not yet implemented")
    }
}

data class TriggerWhere(
    val table: Table,
    val column: Column<*>?
) : Renderer {
    override fun render(): String {
        TODO("Not yet implemented")
    }
}