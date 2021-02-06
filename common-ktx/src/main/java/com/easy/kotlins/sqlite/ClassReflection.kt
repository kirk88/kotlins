package com.easy.kotlins.sqlite.db

import com.easy.kotlins.helper.opt
import com.easy.kotlins.sqlite.SqlColumnCell
import java.lang.reflect.Field
import kotlin.reflect.KClass

internal class FieldWrapper(private val field: Field) {

    fun read(reader: MutableList<SqlColumnCell>, value: Any?) {
        val fieldValue = field.get(value)
        reader.add(
            if (field.isAnnotationPresent(Column::class.java)) {
                val annotation = field.getAnnotation(Column::class.java)
                    ?: throw IllegalStateException("Can not get annotation for column: ${field.name}")
                SqlColumnCell.create(
                    annotation.name.ifEmpty { field.name },
                    if (fieldValue != null) ColumnConverters.get(
                        annotation.converter
                    ).fromValue(fieldValue) else fieldValue
                )
            } else {
                SqlColumnCell.create(
                    field.name,
                    if (field.type == java.lang.Boolean.TYPE || field.type == java.lang.Boolean::class.java)
                        (fieldValue as Boolean? ?: false).opt(1, 0) else fieldValue
                )
            }
        )
    }

}

internal class ReflectAdapter(private val fields: List<FieldWrapper>) {

    fun read(readFrom: Any): Array<SqlColumnCell> {
        val reader = mutableListOf<SqlColumnCell>()
        for (field in fields) {
            field.read(reader, readFrom)
        }
        return reader.toTypedArray()
    }

}

internal object ColumnReflections {

    private val adapters: MutableMap<KClass<out Any>, ReflectAdapter> by lazy { mutableMapOf() }

    fun get(from: Any, ignored: (Field) -> Boolean): Array<SqlColumnCell> {
        return adapters.getOrPut(from::class) {
            val fields = mutableListOf<FieldWrapper>()
            for (field in from.javaClass.declaredFields) {
                if (ignored(field)) continue

                field.isAccessible = true
                fields.add(FieldWrapper(field))
            }
            ReflectAdapter(fields)
        }.read(from)
    }

}