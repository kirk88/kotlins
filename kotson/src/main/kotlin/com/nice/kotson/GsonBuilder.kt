@file:Suppress("unused")

package com.nice.kotson

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

inline fun <reified T : Any> simpleTypeToken(): Type = object : TypeToken<T>() {}.type

inline fun <reified T : Any> typeToken(): Type {
    val type = simpleTypeToken<T>()

    if (type is ParameterizedType && type.isWildcard())
        return type.rawType

    return removeTypeWildcards(type)
}

@PublishedApi
internal fun ParameterizedType.isWildcard(): Boolean {
    var hasAnyWildCard = false
    var hasBaseWildCard = false
    var hasSpecific = false

    val cls = this.rawType as Class<*>
    cls.typeParameters.forEachIndexed { i, variable ->
        val argument = actualTypeArguments[i]

        if (argument is WildcardType) {
            val hit = variable.bounds.firstOrNull { it in argument.upperBounds }
            if (hit != null) {
                if (hit == Any::class.java)
                    hasAnyWildCard = true
                else
                    hasBaseWildCard = true
            } else
                hasSpecific = true
        } else
            hasSpecific = true

    }

    if (hasAnyWildCard && hasSpecific)
        throw IllegalArgumentException("Either none or all type parameters can be wildcard in $this")

    return hasAnyWildCard || (hasBaseWildCard && !hasSpecific)
}

@PublishedApi
internal fun removeTypeWildcards(type: Type): Type {
    if (type is ParameterizedType) {
        val arguments = type.actualTypeArguments
            .map { if (it is WildcardType) it.upperBounds[0] else it }
            .map { removeTypeWildcards(it) }
            .toTypedArray()
        return TypeToken.getParameterized(type.rawType, *arguments).type
    }

    return type
}

data class JsonSerializerContext<T>(
    val src: T,
    val type: Type,
    @PublishedApi internal val baseContext: JsonSerializationContext
) : JsonSerializationContext by baseContext {
    inline fun <reified T : Any> typedSerialize(src: T): JsonElement = baseContext.serialize(src, typeToken<T>())
}

data class JsonDeserializerContext(
    val json: JsonElement,
    val type: Type,
    @PublishedApi internal val baseContext: JsonDeserializationContext
) : JsonDeserializationContext by baseContext {
    inline fun <reified T : Any> typedDeserialize(json: JsonElement): T = baseContext.deserialize(json, typeToken<T>())
}

fun <T : Any> jsonSerializer(serializer: (context: JsonSerializerContext<T>) -> JsonElement): JsonSerializer<T> =
    JsonSerializer { src, type, context -> serializer(JsonSerializerContext(src, type, context)) }

fun <T : Any> jsonDeserializer(deserializer: (context: JsonDeserializerContext) -> T): JsonDeserializer<T> =
    JsonDeserializer<T> { json, type, context -> deserializer(JsonDeserializerContext(json, type, context)) }

fun <T : Any> instanceCreator(creator: (type: Type) -> T): InstanceCreator<T> = InstanceCreator { creator(it) }

interface TypeAdapterBuilder<T : Any?, R : T?> {
    fun read(reader: JsonReader.() -> R)
    fun write(writer: JsonWriter.(value: T) -> Unit)
}

internal class TypeAdapterBuilderImpl<T : Any?, R : T?>(
    init: TypeAdapterBuilder<T, R>.() -> Unit
) : TypeAdapterBuilder<T, R> {

    private var reader: (JsonReader.() -> R)? = null
    private var writer: (JsonWriter.(value: T) -> Unit)? = null

    override fun read(reader: JsonReader.() -> R) {
        this.reader = reader
    }

    override fun write(writer: JsonWriter.(value: T) -> Unit) {
        this.writer = writer
    }

    fun build(): TypeAdapter<T> = object : TypeAdapter<T>() {
        override fun read(reader: JsonReader) = this@TypeAdapterBuilderImpl.reader!!.invoke(reader)
        override fun write(writer: JsonWriter, value: T) = this@TypeAdapterBuilderImpl.writer!!.invoke(writer, value)
    }

    init {
        init()
        check(reader != null || writer != null) {
            "You must define both a read and a write function"
        }
    }
}

fun <T : Any> typeAdapter(init: TypeAdapterBuilder<T, T>.() -> Unit): TypeAdapter<T> = TypeAdapterBuilderImpl(init).build()
fun <T : Any> nullableTypeAdapter(init: TypeAdapterBuilder<T?, T?>.() -> Unit): TypeAdapter<T?> = TypeAdapterBuilderImpl(init).build().nullSafe()

inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(creator: InstanceCreator<T>): GsonBuilder = this.registerTypeAdapter(typeToken<T>(), creator)
inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(serializer: JsonSerializer<T>): GsonBuilder = this.registerTypeAdapter(typeToken<T>(), serializer)
inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(deserializer: JsonDeserializer<T>): GsonBuilder = this.registerTypeAdapter(typeToken<T>(), deserializer)
inline fun <reified T : Any> GsonBuilder.registerTypeAdapter(typeAdapter: TypeAdapter<T>): GsonBuilder = this.registerTypeAdapter(typeToken<T>(), typeAdapter)

inline fun <reified T : Any> GsonBuilder.registerTypeHierarchyAdapter(creator: InstanceCreator<T>): GsonBuilder = this.registerTypeHierarchyAdapter(T::class.java, creator)
inline fun <reified T : Any> GsonBuilder.registerTypeHierarchyAdapter(serializer: JsonSerializer<T>): GsonBuilder = this.registerTypeHierarchyAdapter(T::class.java, serializer)
inline fun <reified T : Any> GsonBuilder.registerTypeHierarchyAdapter(deserializer: JsonDeserializer<T>): GsonBuilder = this.registerTypeHierarchyAdapter(T::class.java, deserializer)
inline fun <reified T : Any> GsonBuilder.registerTypeHierarchyAdapter(typeAdapter: TypeAdapter<T>): GsonBuilder = this.registerTypeHierarchyAdapter(T::class.java, typeAdapter)


fun <T : Any> GsonBuilder.registerNullableTypeAdapterBuilder(type: Type, init: TypeAdapterBuilder<T?, T?>.() -> Unit): GsonBuilder = apply {
    registerTypeAdapter(type, nullableTypeAdapter(init))
}

inline fun <reified T : Any> GsonBuilder.registerNullableTypeAdapter(noinline init: TypeAdapterBuilder<T?, T?>.() -> Unit): GsonBuilder = registerNullableTypeAdapterBuilder(typeToken<T>(), init)

fun <T : Any> GsonBuilder.registerNullableTypeHierarchyAdapterBuilder(type: Class<T>, init: TypeAdapterBuilder<T?, T?>.() -> Unit): GsonBuilder = apply {
    registerTypeHierarchyAdapter(type, nullableTypeAdapter(init))
}

inline fun <reified T : Any> GsonBuilder.registerNullableTypeHierarchyAdapter(noinline init: TypeAdapterBuilder<T?, T?>.() -> Unit): GsonBuilder =
    registerNullableTypeHierarchyAdapterBuilder(T::class.java, init)
