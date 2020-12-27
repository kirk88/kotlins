package com.easy.kotlins.sqlite

import kotlin.reflect.KClass

object ColumnConverters {
    private val converters: MutableMap<KClass<out ColumnConverter<*, *>>, ColumnConverter<Any, Any>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun get(clazz: KClass<out ColumnConverter<*, *>>): ColumnConverter<Any, Any>{
        return converters.getOrPut(clazz) { clazz.java.newInstance() as ColumnConverter<Any, Any>}
    }
}