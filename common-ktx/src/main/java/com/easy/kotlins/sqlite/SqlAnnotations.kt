package com.easy.kotlins.sqlite.db

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class TableClass

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ClassParserConstructor

@Target(AnnotationTarget.FIELD)
annotation class IgnoredOnTable

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Column(
    val name: String = "",
    val converter: KClass<out ValueConverter<out Any, out Any>> = DefaultValueConverter::class
)