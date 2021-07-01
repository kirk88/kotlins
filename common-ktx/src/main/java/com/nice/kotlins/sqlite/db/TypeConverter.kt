package com.nice.kotlins.sqlite.db

import kotlin.reflect.KClass

interface ColumnValueConverter<T : Any, R : Any> {
    fun toDatabaseValue(value: T?): R?

    fun toPropertyValue(value: R?): T?
}

class DefaultColumnValueConverter : ColumnValueConverter<Any, Any> {
    override fun toDatabaseValue(value: Any?): Any? = value
    override fun toPropertyValue(value: Any?): Any? = value
}

internal object ColumnConverters {

    private val CONVERTERS: MutableMap<KClass<out ColumnValueConverter<*, *>>, ColumnValueConverter<Any, Any>> by lazy { mutableMapOf() }

    fun get(type: KClass<out ColumnValueConverter<*, *>>): ColumnValueConverter<Any, Any> {
        @Suppress("UNCHECKED_CAST")
        return CONVERTERS.getOrPut(type) { type.java.newInstance() as ColumnValueConverter<Any, Any> }
    }

}