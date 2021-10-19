@file:Suppress("unused")

package com.nice.common.event

interface Event

open class NamedEvent(open val name: String, open val value: Any) : Event{

    override fun toString(): String = "NamedEvent(name='$name', value=$value)"

}

operator fun NamedEvent.component1(): String = name
operator fun NamedEvent.component2(): Any = value