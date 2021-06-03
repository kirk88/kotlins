package com.nice.kotlins.sqlite.db

import com.nice.kotlins.helper.ifNullOrEmpty
import java.lang.reflect.Field

internal class FieldWrapper(private val field: Field) {

    private val column: Column?
    private val isBooleanType: Boolean

    val name: String

    init {
        field.isAccessible = true

        column = field.getAnnotation(Column::class.java)
        isBooleanType = field.type == java.lang.Boolean.TYPE
                || field.type == java.lang.Boolean::class.java

        name = column?.name.ifNullOrEmpty { field.name }
    }

    fun read(reader: Any, values: MutableList<SqlColumnElement>) {
        val fieldValue = field.get(reader)
        values.add(
            if (column != null) {
                SqlColumnElement.create(
                    name,
                    if (fieldValue != null) {
                        ColumnConverters.get(
                            column.converter
                        ).fromValue(fieldValue)
                    } else {
                        fieldValue
                    }
                )
            } else {
                SqlColumnElement.create(
                    name,
                    if (isBooleanType) {
                        if (fieldValue as Boolean? == true) 1 else 0
                    } else {
                        fieldValue
                    }
                )
            }
        )
    }

    fun write(writer: Any, value: SqlColumnValue) {
        if (!value.isNull() && column != null) {
            field.set(
                writer,
                ColumnConverters.get(
                    column.converter
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

    fun read(reader: Any): List<SqlColumnElement> {
        val values = mutableListOf<SqlColumnElement>()
        for (field in fields) {
            field.value.read(reader, values)
        }
        return values
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
                val fieldWrapper = FieldWrapper(field)
                fields[fieldWrapper.name] = fieldWrapper
            }
            ReflectAdapter(fields)
        }
    }

}