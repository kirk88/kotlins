@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier


@Suppress("NOTHING_TO_INLINE")
inline fun <reified T : Any> classParser(): RowParser<T> = classParser(T::class.java)

@PublishedApi
internal fun <T> classParser(clazz: Class<T>): RowParser<T> = ClassParsers.get(clazz)

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

internal class ClassParser<T>(clazz: Class<T>) : RowParser<T> {

    private val preferredConstructor: Constructor<*>
    private val parameterAnnotations: Array<Array<Annotation>>
    private val parameterTypes: Array<Class<*>>

    init {
        val applicableConstructors = clazz.declaredConstructors.filter { ctr ->
            if (ctr.isVarArgs || !Modifier.isPublic(ctr.modifiers)) return@filter false
            val types = ctr.parameterTypes.zip(ctr.parameterAnnotations) { type, annotations ->
                type to annotations
            }
            return@filter types.isNotEmpty() && types.all(::hasApplicableType)
        }

        if (applicableConstructors.isEmpty()) {
            throw IllegalStateException("Can't initialize object parser for ${clazz.canonicalName}, no acceptable constructors found")
        }

        preferredConstructor = if (applicableConstructors.size > 1) {
            applicableConstructors.singleOrNull { it.isAnnotationPresent(ClassParserConstructor::class.java) }
                ?: throw IllegalStateException("Several constructors are annotated with ClassParserConstructor")
        } else {
            applicableConstructors[0]
        }

        parameterAnnotations = preferredConstructor.parameterAnnotations
        parameterTypes = preferredConstructor.parameterTypes
    }

    override fun parseRow(row: Array<ColumnElement>): T {
        if (parameterTypes.size != row.size) {
            val columnsRendered = row.joinToString(prefix = "[", postfix = "]")
            val parameterTypesRendered =
                parameterTypes.joinToString(prefix = "[", postfix = "]") { it.name }
            throw IllegalArgumentException(
                "Class parser for ${preferredConstructor.name} " +
                        "failed to parse the row: $columnsRendered (constructor parameter types: $parameterTypesRendered)"
            )
        }

        val args = arrayOfNulls<Any>(parameterTypes.size)

        for (index in parameterTypes.indices) {
            val type = parameterTypes[index]
            val annotations = parameterAnnotations[index]
            val column = row[index]

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

internal object ClassParsers {

    private val parsers: MutableMap<Class<*>, ClassParser<*>> by lazy { mutableMapOf() }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: Class<T>): ClassParser<T> {
        return parsers.getOrPut(type) {
            ClassParser(type)
        } as ClassParser<T>
    }

}
