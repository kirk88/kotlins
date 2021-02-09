package com.easy.kotlins.sqlite.db

import com.easy.kotlins.helper.opt
import com.easy.kotlins.sqlite.SqlColumnElement
import java.lang.reflect.Field
import kotlin.reflect.KClass

internal class FieldWrapper(private val field: Field) {

    fun read(reader: Any, values: MutableList<SqlColumnElement>) {
        val fieldValue = field.get(reader)
        values.add(
            if (field.isAnnotationPresent(Column::class.java)) {
                val annotation = field.getAnnotation(Column::class.java)
                    ?: throw IllegalStateException("Can not get annotation for column: ${field.name}")
                SqlColumnElement.create(
                    annotation.name.ifEmpty { field.name },
                    if (fieldValue != null) ColumnConverters.get(
                        annotation.converter
                    ).fromValue(fieldValue) else fieldValue
                )
            } else {
                SqlColumnElement.create(
                    field.name,
                    if (field.type == java.lang.Boolean.TYPE || field.type == java.lang.Boolean::class.java)
                        (fieldValue as Boolean? ?: false).opt(1, 0) else fieldValue
                )
            }
        )
    }

    fun write(writer: Any, value: SqlColumnValue) {
        if (!value.isNull() && field.isAnnotationPresent(Column::class.java)) {
            val annotation = field.getAnnotation(Column::class.java)
                ?: throw IllegalStateException("Can not get annotation for column: ${field.name}")

            field.set(
                writer,
                ColumnConverters.get(
                    annotation.converter
                ).toValue(value)
            )
        } else if (field.type == java.lang.Boolean.TYPE || field.type == java.lang.Boolean::class.java) {
            field.set(writer, value.asInt() == 1)
        } else {
            field.set(writer, value.asTyped(field.type))
        }
    }

}

internal class ReflectAdapter(private val fields: Map<String, FieldWrapper>) {

    fun read(reader: Any): Array<SqlColumnElement> {
        val values = mutableListOf<SqlColumnElement>()
        for (field in fields) {
            field.value.read(reader, values)
        }
        return values.toTypedArray()
    }

    fun write(writer: Any, values: Map<String, SqlColumnValue>) {
        for ((name, value) in values) {
            val field = fields[name] ?: continue
            field.write(writer, value)
        }
    }
}

internal object ClassReflections {

    private val adapters: MutableMap<KClass<out Any>, ReflectAdapter> by lazy { mutableMapOf() }

    fun getAdapter(value: Any, ignored: (Field) -> Boolean): ReflectAdapter {
        return adapters.getOrPut(value::class) {
            val fields = mutableMapOf<String, FieldWrapper>()
            for (field in value.javaClass.declaredFields) {
                if (ignored(field)) continue

                field.isAccessible = true
                fields[field.name] = FieldWrapper(field)
            }
            ReflectAdapter(fields)
        }
    }

}