package com.nice.sqlite

import java.lang.reflect.Field
import java.lang.reflect.Modifier

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ClassParserConstructor

@Target(AnnotationTarget.FIELD)
annotation class IgnoreOnTable

internal class FieldWrapper(private val field: Field) {

    val name: String = field.name

    init {
        field.isAccessible = true
    }

    fun write(writer: Any, element: ColumnElement) {
        field.set(writer, element.asTyped(field.type))
    }

}

internal class ReflectAdapter(private val fields: Map<String, FieldWrapper>) {

    fun write(writer: Any, row: Row) {
        for (element in row) {
            val field = fields[element.name] ?: continue
            field.write(writer, element)
        }
    }
}

internal object ClassReflections {

    private val adapters: MutableMap<Class<*>, ReflectAdapter> by lazy { mutableMapOf() }

    fun getAdapter(
        clazz: Class<out Any>,
        ignored: (Field) -> Boolean = {
            Modifier.isTransient(it.modifiers)
                    || Modifier.isStatic(it.modifiers)
                    || it.isAnnotationPresent(IgnoreOnTable::class.java)
        }
    ): ReflectAdapter {
        return adapters.getOrPut(clazz) {
            val fields = mutableMapOf<String, FieldWrapper>()
            for (field in clazz.declaredFields) {
                if (ignored(field)) continue
                val fieldWrapper = FieldWrapper(field)
                fields[fieldWrapper.name] = fieldWrapper
            }
            ReflectAdapter(fields)
        }
    }

}