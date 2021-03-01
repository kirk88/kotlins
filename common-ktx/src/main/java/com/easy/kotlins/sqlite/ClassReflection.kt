package com.easy.kotlins.sqlite.db

import com.easy.kotlins.helper.opt
import com.easy.kotlins.sqlite.SqlColumnElement
import java.lang.reflect.Field

internal class FieldWrapper(private val field: Field) {

    private val columnAnnotation = field.getAnnotation(Column::class.java)
    private val isBooleanType =
        field.type == java.lang.Boolean.TYPE || field.type == java.lang.Boolean::class.java

    fun read(reader: Any, values: MutableList<SqlColumnElement>) {
        val fieldValue = field.get(reader)
        values.add(
            if (columnAnnotation != null) {
                SqlColumnElement.create(
                    columnAnnotation.name.ifEmpty { field.name },
                    if (fieldValue != null) ColumnConverters.get(
                        columnAnnotation.converter
                    ).fromValue(fieldValue) else fieldValue
                )
            } else {
                SqlColumnElement.create(
                    field.name,
                    if (isBooleanType) (fieldValue as Boolean? ?: false).opt(1, 0) else fieldValue
                )
            }
        )
    }

    fun write(writer: Any, value: SqlColumnValue) {
        if (!value.isNull() && columnAnnotation != null) {
            field.set(
                writer,
                ColumnConverters.get(
                    columnAnnotation.converter
                ).toValue(value.value!!)
            )
        } else if (isBooleanType) {
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

    private val adapters: MutableMap<Class<*>, ReflectAdapter> by lazy { mutableMapOf() }

    fun getAdapter(clazz: Class<out Any>, ignored: (Field) -> Boolean): ReflectAdapter {
        return adapters.getOrPut(clazz) {
            val fields = mutableMapOf<String, FieldWrapper>()
            for (field in clazz.declaredFields) {
                if (ignored(field)) continue

                field.isAccessible = true
                fields[field.name] = FieldWrapper(field)
            }
            ReflectAdapter(fields)
        }
    }

}