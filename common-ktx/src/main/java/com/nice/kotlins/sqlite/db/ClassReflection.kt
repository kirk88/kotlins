package com.nice.kotlins.sqlite.db

import com.nice.kotlins.helper.ifNullOrEmpty
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal class FieldWrapper(private val field: Field) {

    val name: String
    private val converter: ColumnValueConverter<Any, Any>?

    init {
        field.isAccessible = true

        val annotation = field.getAnnotation(Column::class.java)
        name = annotation?.name.ifNullOrEmpty { field.name }
        converter = annotation?.let { ColumnConverters.get(it.converterClass) }
    }

    fun read(reader: Any, values: MutableList<SqlColumnElement>) {
        val value = field.get(reader)
        values.add(
            SqlColumnElement.create(
                name,
                if (converter != null) converter.toDatabaseValue(value) else value
            )
        )
    }

    fun write(writer: Any, value: SqlColumnValue) {
        if (converter != null) {
            field.set(writer, converter.toPropertyValue(value.value))
        } else {
            field.set(writer, value.asTyped(field.type))
        }
    }

}

internal class ReflectAdapter(private val fields: Map<String, FieldWrapper>) {

    fun read(reader: Any): List<SqlColumnElement> {
        val values = mutableListOf<SqlColumnElement>()
        for (field in fields.values) {
            field.read(reader, values)
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