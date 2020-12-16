/*
 * Copyright 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.easy.kotlins.sqlite.db

import com.easy.kotlins.sqlite.ClassParserConstructor
import com.easy.kotlins.sqlite.Column
import com.easy.kotlins.sqlite.ColumnConverter
import com.easy.kotlins.sqlite.IgnoreInTable
import com.easy.kotlins.sqlite.db.JavaSqliteUtils.PRIMITIVES_TO_WRAPPERS
import java.lang.reflect.Modifier

@Suppress("NOTHING_TO_INLINE")
inline fun <reified T : Any> classParser(): RowParser<T> = classParser(T::class.java)

@PublishedApi
internal fun <T> classParser(clazz: Class<T>): RowParser<T> {
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

    val preferredConstructor = if (applicableConstructors.size > 1) {
        applicableConstructors.singleOrNull { it.isAnnotationPresent(ClassParserConstructor::class.java) }
            ?: throw IllegalStateException("Several constructors are annotated with ClassParserConstructor")
    } else {
        applicableConstructors[0]
    }

    return object : RowParser<T> {
        private val parameterAnnotations = preferredConstructor.parameterAnnotations
        private val parameterTypes = preferredConstructor.parameterTypes.filterIndexed { index, _ ->
            parameterAnnotations[index].none { it is IgnoreInTable }
        }

        @Suppress("UNCHECKED_CAST")
        override fun parseRow(columns: Array<Any?>): T {
            if (parameterTypes.size != columns.size) {
                val columnsRendered = columns.joinToString(prefix = "[", postfix = "]")
                val parameterTypesRendered =
                    parameterTypes.joinToString(prefix = "[", postfix = "]") { it.name }
                throw IllegalArgumentException(
                    "Class parser for ${preferredConstructor.name} " +
                            "failed to parse the row: $columnsRendered (constructor parameter types: $parameterTypesRendered)"
                )
            }

            for (index in parameterTypes.indices) {
                val type = parameterTypes[index]
                val annotations = parameterAnnotations[index]
                val columnValue = columns[index]

                val column = annotations.find { it is Column } as Column?
                if (columnValue != null && column != null) {
                    val converter = column.converter.java.newInstance() as ColumnConverter<Any, Any>
                    columns[index] = converter.toValue(columnValue)
                } else if (!type.isInstance(columnValue)) {
                    columns[index] = castValue(columnValue, type)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return (JavaSqliteUtils.newInstance(preferredConstructor, columns)) as T
        }
    }
}

private fun hasApplicableType(type: Pair<Class<*>, Array<Annotation>>): Boolean {
    if (type.first.isPrimitive || type.second.any { it is IgnoreInTable }) {
        return true
    }

    return when (type.first) {
        java.lang.String::class.java, java.lang.CharSequence::class.java,
        java.lang.Long::class.java, java.lang.Integer::class.java,
        java.lang.Byte::class.java, java.lang.Character::class.java,
        java.lang.Boolean::class.java, java.lang.Float::class.java,
        java.lang.Double::class.java, java.lang.Short::class.java -> true
        else -> type.second.any { it is Column }
    }
}

private fun castValue(value: Any?, type: Class<*>): Any? {
    if (value == null && type.isPrimitive) {
        throw IllegalArgumentException("null can't be converted to the value of primitive type ${type.canonicalName}")
    }

    if (value == null || type == Any::class.java) {
        return value
    }

    if (type.isPrimitive && PRIMITIVES_TO_WRAPPERS[type] == value::class.java) {
        return value
    }

    if (value is Double && (type == java.lang.Float.TYPE || type == java.lang.Float::class.java)) {
        return value.toFloat()
    }

    if (value is Float && (type == java.lang.Double.TYPE || type == java.lang.Double::class.java)) {
        return value.toDouble()
    }

    if (value is Char && CharSequence::class.java.isAssignableFrom(type)) {
        return value.toString()
    }

    if (value is Long) {
        if (type == java.lang.Integer.TYPE || type == java.lang.Integer::class.java) {
            return value.toInt()
        } else if (type == java.lang.Short.TYPE || type == java.lang.Short::class.java) {
            return value.toShort()
        } else if (type == java.lang.Byte.TYPE || type == java.lang.Byte::class.java) {
            return value.toByte()
        } else if (type == java.lang.Boolean.TYPE || type == java.lang.Boolean::class.java) {
            return value != 0L
        } else if (type == java.lang.Character.TYPE || type == java.lang.Character::class.java) {
            return value.toChar()
        }
    }

    if (value is Int) {
        if (type == java.lang.Long.TYPE || type == java.lang.Long::class.java) {
            return value.toLong()
        } else if (type == java.lang.Short.TYPE || type == java.lang.Short::class.java) {
            return value.toShort()
        } else if (type == java.lang.Byte.TYPE || type == java.lang.Byte::class.java) {
            return value.toByte()
        } else if (type == java.lang.Boolean.TYPE || type == java.lang.Boolean::class.java) {
            return value != 0
        } else if (type == java.lang.Character.TYPE || type == java.lang.Character::class.java) {
            return value.toChar()
        }
    }

    if (value is String && value.length == 1
        && (type == java.lang.Character.TYPE || type == java.lang.Character::class.java)
    ) {
        return value[0]
    }

    throw IllegalArgumentException("Value $value of type ${value::class.java} can't be cast to ${type.canonicalName}")
}