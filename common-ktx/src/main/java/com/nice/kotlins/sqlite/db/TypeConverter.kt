package com.nice.kotlins.sqlite.db

import kotlin.reflect.KClass

interface ColumnValueConverter<T : Any, R : Any> {
    fun fromValue(value: T): R

    fun toValue(value: R): T
}

class DefaultColumnValueConverter : ColumnValueConverter<Any, Any> {
    override fun fromValue(value: Any): Any = value
    override fun toValue(value: Any): Any = value
}

internal object ColumnConverters {

    private val CONVERTERS: MutableMap<KClass<out ColumnValueConverter<*, *>>, ColumnValueConverter<Any, Any>> by lazy { mutableMapOf() }

    fun get(type: KClass<out ColumnValueConverter<*, *>>): ColumnValueConverter<Any, Any> {
        @Suppress("UNCHECKED_CAST")
        return CONVERTERS.getOrPut(type) { type.java.newInstance() as ColumnValueConverter<Any, Any> }
    }

}