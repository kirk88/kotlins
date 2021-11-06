package com.nice.sqlite

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

inline fun <reified T : Any> classParser(): RowParser<T> = ClassParsers.get(T::class.java)

private fun isApplicableType(type: Class<*>): Boolean {
    if (type.isPrimitive) {
        return true
    }

    return when (type) {
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

    private val delegate: RowParser<T>

    init {
        val constructors = clazz.declaredConstructors

        delegate =
            if (constructors.none { it.isAnnotationPresent(ClassParserConstructor::class.java) }) {
                val constructor =
                    requireNotNull(constructors.find { it.parameterTypes.isEmpty() }) {
                        "Can't initialize object parser for ${clazz.canonicalName}, no empty constructor found"
                    }

                ClassFieldParser(constructor)
            } else {
                val applicableConstructors = constructors.filter { ctr ->
                    if (ctr.isVarArgs || !Modifier.isPublic(ctr.modifiers)) return@filter false
                    val types = ctr.parameterTypes
                    return@filter types.isNotEmpty() && types.all(::isApplicableType)
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

    override fun parse(row: Row): T {
        return delegate.parse(row)
    }
}

internal class ClassConstructorParser<T>(
    private val constructor: Constructor<*>
) : RowParser<T> {

    private val parameterTypes: Array<Class<*>> = constructor.parameterTypes

    override fun parse(row: Row): T {
        if (parameterTypes.size != row.size) {
            val columnsRendered = row.joinToString(prefix = "[", postfix = "]") { it.value.toString() }
            val parameterTypesRendered =
                parameterTypes.joinToString(prefix = "[", postfix = "]") { it.name }
            throw IllegalArgumentException(
                "Class parser for ${constructor.name} failed to parse the row: $columnsRendered (constructor parameter types: $parameterTypesRendered)"
            )
        }

        val args = arrayOfNulls<Any>(parameterTypes.size)

        for ((index, type) in parameterTypes.withIndex()) {
            val element = row[index]
            args[index] = element.asTyped(type)
        }

        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(*args) as T
    }

}

internal class ClassFieldParser<T>(
    private val constructor: Constructor<*>
) : RowParser<T> {

    private val adapter = ClassReflections.getAdapter(constructor.declaringClass)

    override fun parse(row: Row): T {
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance().also { adapter.write(it, row) } as T
    }

}

@PublishedApi
internal object ClassParsers {

    private val parsers: MutableMap<Class<*>, ClassParser<*>> by lazy { mutableMapOf() }

    fun <T> get(clazz: Class<T>): ClassParser<T> {
        @Suppress("UNCHECKED_CAST")
        return parsers.getOrPut(clazz) { ClassParser(clazz) } as ClassParser<T>
    }

}