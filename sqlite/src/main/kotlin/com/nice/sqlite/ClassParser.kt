@file:Suppress("unused")

package com.nice.sqlite

import com.nice.sqlite.*
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ClassParserConstructor

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Column(
    val name: String = "",
    val converterClass: KClass<out ColumnValueConverter<out Any, out Any>> = DefaultColumnValueConverter::class
)

@Target(AnnotationTarget.FIELD)
annotation class IgnoreOnTable

inline fun <reified T : Any> classParser(): MapRowParser<T> = ClassParsers.get(T::class.java)

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

        delegate = if (constructors.none { it.isAnnotationPresent(ClassParserConstructor::class.java) }) {
            val constructor = requireNotNull(constructors.find { it.parameterTypes.isEmpty() }) {
                "Can't initialize object parser for ${clazz.canonicalName}, no empty constructor found"
            }

            ClassFieldParser(constructor)
        } else {
            val applicableConstructors = constructors.filter { ctr ->
                if (ctr.isVarArgs || !Modifier.isPublic(ctr.modifiers)) return@filter false
                val types = ctr.parameterTypes.zip(ctr.parameterAnnotations) { type, annotations ->
                    type to annotations
                }
                return@filter types.isNotEmpty() && types.all(::hasApplicableType)
            }

            check(applicableConstructors.isNotEmpty()) {
                "Can't initialize object parser for ${clazz.canonicalName}, no acceptable constructors found"
            }

            val preferredConstructor = if (applicableConstructors.size > 1) {
                requireNotNull(applicableConstructors.singleOrNull {
                    it.isAnnotationPresent(ClassParserConstructor::class.java)
                }) {
                    "Several constructors are annotated with ClassParserConstructor"
                }
            } else {
                applicableConstructors[0]
            }

            ClassConstructorParser(preferredConstructor)
        }
    }

    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        return delegate.parseRow(row)
    }
}

internal class ClassConstructorParser<T>(
    private val constructor: Constructor<*>
) : MapRowParser<T> {

    val parameters = constructor.parameterTypes.zip(constructor.parameterAnnotations) { type, annotations ->
        type to annotations.filterIsInstance<Column>().singleOrNull()?.let {
            ColumnConverters.get(it.converterClass)
        }
    }

    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        if (parameters.size != row.size) {
            val columnsRendered = row.values.joinToString(prefix = "[", postfix = "]")
            val parameterTypesRendered = parameters.joinToString(prefix = "[", postfix = "]") { it.first.name }
            throw IllegalArgumentException(
                "Class parser for ${constructor.name} failed to parse the row: $columnsRendered (constructor parameter types: $parameterTypesRendered)"
            )
        }

        val args = arrayOfNulls<Any>(parameters.size)

        for ((index, parameter) in parameters.withIndex()) {
            val value = row.values.elementAt(index)
            val type = parameter.first
            val converter = parameter.second
            if (converter == null) {
                args[index] = value.asTyped(type)
            } else {
                args[index] = converter.toPropertyValue(value.value)
            }
        }

        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(*args) as T
    }

}

internal class ClassFieldParser<T>(
    private val constructor: Constructor<*>
) : MapRowParser<T> {

    val adapter = ClassReflections.getAdapter(constructor.declaringClass)

    override fun parseRow(row: Map<String, SqlColumnValue>): T {
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance().also {
            adapter.write(it, row)
        } as T
    }

}

@PublishedApi
internal object ClassParsers {

    private val parsers: MutableMap<Class<*>, ClassParser<*>> by lazy { mutableMapOf() }

    fun <T> get(clazz: Class<T>): ClassParser<T> {
        @Suppress("UNCHECKED_CAST")
        return parsers.getOrPut(clazz) {
            ClassParser(clazz)
        } as ClassParser<T>
    }

}