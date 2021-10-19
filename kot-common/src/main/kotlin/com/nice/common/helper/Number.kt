@file:Suppress("unused")

package com.nice.common.helper

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

fun Float.scale(newScale: Int, roundingMode: RoundingMode = RoundingMode.HALF_UP): Float {
    return this.toBigDecimal().setScale(newScale, roundingMode).toFloat()
}

fun Double.scale(newScale: Int, roundingMode: RoundingMode = RoundingMode.HALF_UP): Double {
    return this.toBigDecimal().setScale(newScale, roundingMode).toDouble()
}

fun Long.between(min: Long, max: Long): Long {
    return min(max, max(this, min))
}

fun Int.between(min: Int, max: Int): Int {
    return min(max, max(this, min))
}

fun Float.between(min: Float, max: Float): Float {
    return min(max, max(this, min))
}

fun Double.between(min: Double, max: Double): Double {
    return min(max, max(this, min))
}

fun <T : Number> T?.orZero(): T {
    @Suppress("UNCHECKED_CAST")
    return this ?: when (this) {
        is Int? -> 0
        is Long? -> 0.toLong()
        is Float? -> 0.toFloat()
        is Double? -> 0.toDouble()
        is Short? -> 0.toShort()
        is Byte? -> 0.toByte()
        is BigDecimal? -> 0.toBigDecimal()
        is BigInteger? -> 0.toBigInteger()
        else -> throw IllegalArgumentException("Wrong number type: $this")
    } as T
}

inline fun <T : Number> T.ifZero(defaultValue: () -> T): T = if (this.toInt() != 0) this else defaultValue()

inline fun <T : Number> T?.ifNullOrZero(defaultValue: () -> T): T = if (this != null && this.toInt() != 0) this else defaultValue()

fun Number.toTrimmedString(): String = when (this) {
    is BigDecimal -> stripTrailingZeros().toPlainString()
    is Double -> toBigDecimal().stripTrailingZeros().toPlainString()
    is Float -> toBigDecimal().stripTrailingZeros().toPlainString()
    else -> toString()
}