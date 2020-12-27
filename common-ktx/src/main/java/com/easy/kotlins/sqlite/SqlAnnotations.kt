package com.easy.kotlins.sqlite

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class TableClass

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ClassParserConstructor

@Repeatable
@Retention
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
annotation class IgnoredOnTable

@Repeatable
@Retention
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
annotation class Column(
    val name: String = "",
    val converter: KClass<out ColumnConverter<out Any, out Any>> = DefaultColumnConverter::class
)

interface ColumnConverter<T : Any, R : Any> {
    fun fromValue(value: T): R

    fun toValue(value: R): T
}

class DefaultColumnConverter : ColumnConverter<Any, Any> {
    override fun fromValue(value: Any): Any = value
    override fun toValue(value: Any): Any = value
}
