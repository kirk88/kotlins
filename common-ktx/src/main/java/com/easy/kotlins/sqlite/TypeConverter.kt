package com.easy.kotlins.sqlite.db

import kotlin.reflect.KClass
import kotlin.reflect.cast

interface ValueConverter<T : Any, R : Any> {
    fun fromValue(value: T): R

    fun toValue(value: R): T
}

class DefaultValueConverter : ValueConverter<Any, Any> {
    override fun fromValue(value: Any): Any = value
    override fun toValue(value: Any): Any = value
}

internal object ColumnConverters {

    private val CONVERTERS: MutableMap<KClass<out ValueConverter<*, *>>, ValueConverter<Any, Any>> by lazy { mutableMapOf() }

    fun get(type: KClass<out ValueConverter<*, *>>): ValueConverter<Any, Any>{
        @Suppress("UNCHECKED_CAST")
        return CONVERTERS.getOrPut(type){  type.java.newInstance() as ValueConverter<Any, Any>  }
    }

}