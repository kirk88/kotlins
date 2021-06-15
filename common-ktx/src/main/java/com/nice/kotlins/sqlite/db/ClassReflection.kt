package com.nice.kotlins.sqlite.db

import com.nice.kotlins.helper.ifNullOrEmpty
import java.lang.reflect.Field

internal class FieldWrapper(private val field: Field) {

    private val valueConverter: ColumnValueConverter<Any, Any>?
    private val isBooleanType: Boolean

    val name: String

    init {
        field.isAccessible = true

        val column = field.getAnnotation(Column::class.java)
        valueConverter = if (column == null) null else ColumnConverters.get(column.converter)
        isBooleanType = field.type == java.lang.Boolean.TYPE
                || field.type == java.lang.Boolean::class.java
        name = column?.name.ifNullOrEmpty { field.name }
    }

    fun read(reader: Any, values: MutableList<SqlColumnElement>) {
        val value = field.get(reader)
        values.add(
            SqlColumnElement.create(
                name,
                when {
                    valueConverter != null && value != null -> valueConverter.fromValue(value)
                    isBooleanType -> if (value as Boolean? == true) 1 else 0
                    else -> value
                }
            )
        )
    }

    fun write(writer: Any, value: SqlColumnValue) {
        when {
            !value.isNull() && valueConverter != null -> field.set(writer,
                valueConverter.toValue(value.value!!))
            isBooleanType -> field.set(writer, value.asInt() == 1)
            else -> field.set(writer, value.asTyped(field.type))
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