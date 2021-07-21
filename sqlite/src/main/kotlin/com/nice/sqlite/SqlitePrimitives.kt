package com.nice.sqlite

import java.util.*

internal object SqlitePrimitives {
    val PRIMITIVES_TO_WRAPPERS: MutableMap<Class<*>?, Class<*>>

    init {
        PRIMITIVES_TO_WRAPPERS = HashMap()
        PRIMITIVES_TO_WRAPPERS[Boolean::class.javaPrimitiveType] = Boolean::class.java
        PRIMITIVES_TO_WRAPPERS[Byte::class.javaPrimitiveType] = Byte::class.java
        PRIMITIVES_TO_WRAPPERS[Char::class.javaPrimitiveType] = Char::class.java
        PRIMITIVES_TO_WRAPPERS[Double::class.javaPrimitiveType] = Double::class.java
        PRIMITIVES_TO_WRAPPERS[Float::class.javaPrimitiveType] = Float::class.java
        PRIMITIVES_TO_WRAPPERS[Int::class.javaPrimitiveType] = Int::class.java
        PRIMITIVES_TO_WRAPPERS[Long::class.javaPrimitiveType] = Long::class.java
        PRIMITIVES_TO_WRAPPERS[Short::class.javaPrimitiveType] = Short::class.java
        PRIMITIVES_TO_WRAPPERS[Void.TYPE] = Void::class.java
    }
}