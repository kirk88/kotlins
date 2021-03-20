@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ClassParserConstructor

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Column(
    val name: String = "",
    val converter: KClass<out ColumnValueConverter<out Any, out Any>> = DefaultColumnValueConverter::class
)

@Target(AnnotationTarget.CLASS)
annotation class DatabaseTable

@Target(AnnotationTarget.FIELD)
annotation class IgnoreOnTable

inline fun <reified T : Any> classParser(): MapRowParser<T> = classParser(T::class.java)

@PublishedApi
internal fun <T> classParser(clazz: Class<T>): MapRowParser<T> = ClassParsers.get(clazz)

private fun hasApplicableType(type: Pair<Class<*>, Array<Annotation>>): Boolean {
    if (type.first.isPrimitive || type.second.any { it is Column }) {
        return true
    }

    return when (type.first) {
        java.lang.String::class.java, java.lang.CharSequence::class.java,
        java.lang.Long::class.java, java.lang.Integer::class.java,
        java.lang.Byte::class.java, java.lang.Character::class.java,
        java.lang.Boolean::class.java, java.lang.Float::class.java,
        java.lang.Double::class.java, java.lang.Short::class.java,
        ByteArray::class.java -> true
        else -> false
    }
}

internal class ClassParser<T>(clazz: Class<T>) : MapRowParser<T> {

    private val delegate: MapRowParser<T>

    init {
        val constructors = clazz.declaredConstructors

        if (constructors.none { it.isAnnotationPresent(ClassParserConstructor::class.java) }) {
            val constructor = constructors.find { it.parameterTypes.isEmpty() }
                ?: throw IllegalStateException("Can't initialize object parser for ${clazz.canonicalName}, no acceptable constructors found")

            delegate = ClassFieldParser(constructor)
        } else {
            val applicableConstructors = constructors.filter { ctr ->
                if (ctr.isVarArgs || !Modifier.isPublic(ctr.modifiers)) return@filter false
                val types = ctr.parameterTypes.zip(ctr.parameterAnnotations) { type, annotations ->
                    type to annotations
                }
                return@filter types.isNotEmpty() && types.all(::hasApplicableType)
            }

            if (applicableConstructors.isEmpty()) {
                throw IllegalStateException("Can't initialize object parser for ${clazz.canonicalName}, no acceptable constructors found")
            }

            val preferredConstructor = if (applicableConstructors.size > 1) {
                applicableConstructors.singleOrNull { it.isAnnotationPresent(ClassParserConstructor::class.java) }
                    ?: throw IllegalStateException("Several constructors are annotated with ClassParserConstructor")
            } else {
                applicableConstructors[0]
            }

            val parameterAnnotations = preferredConstructor.parameterAnnotations
            val parameterTypes = preferredConstructor.parameterTypes

            delegate = ClassConstructorParser(
                preferredConstructor,
                parameterAnnotations,
                parameterTypes
            )
        }
    }

    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        return delegate.parseRow(row)
    }
}

internal class ClassConstructorParser<T>(
    private val preferredConstructor: Constructor<*>,
    private val parameterAnnotations: Array<Array<Annotation>>,
    private val parameterTypes: Array<Class<*>>
) : MapRowParser<T> {

    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        if (parameterTypes.size != row.size) {
            val columnsRendered = row.values.joinToString(prefix = "[", postfix = "]")
            val parameterTypesRendered =
                parameterTypes.joinToString(prefix = "[", postfix = "]") { it.name }
            throw IllegalArgumentException(
                "Class parser for ${preferredConstructor.name} " +
                        "failed to parse the row: $columnsRendered (constructor parameter types: $parameterTypesRendered)"
            )
        }

        val args = arrayOfNulls<Any>(parameterTypes.size)

        for ((index, column) in row.values.withIndex()) {
            val type = parameterTypes[index]
            val annotations = parameterAnnotations[index]

            val annotation = annotations.find { it is Column } as Column?
            if (!column.isNull() && annotation != null) {
                args[index] = ColumnConverters.get(annotation.converter).toValue(column.value!!)
            } else {
                args[index] = column.asTyped(type)
            }
        }

        @Suppress("UNCHECKED_CAST")
        return preferredConstructor.newInstance(*args) as T
    }

}

internal class ClassFieldParser<T>(
    private val converter: Constructor<*>
) : MapRowParser<T> {
    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        val target = converter.newInstance()
        ClassReflections.getAdapter(target.javaClass) {
            Modifier.isTransient(it.modifiers)
                    || Modifier.isStatic(it.modifiers)
                    || it.isAnnotationPresent(IgnoreOnTable::class.java)
        }.write(target, row)
        @Suppress("UNCHECKED_CAST")
        return target as T
    }

}

internal object ClassParsers {

    private val parsers: MutableMap<Class<*>, ClassParser<*>> by lazy { mutableMapOf() }

    fun <T> get(type: Class<T>): ClassParser<T> {
        @Suppress("UNCHECKED_CAST")
        return parsers.getOrPut(type) {
            ClassParser(type)
        } as ClassParser<T>
    }

}