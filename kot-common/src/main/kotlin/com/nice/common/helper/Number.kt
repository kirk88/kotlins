@file:Suppress("UNUSED")

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

fun BigDecimal.toTrimmedString(): String = stripTrailingZeros().toPlainString()

fun Double.toTrimmedString(): String = toBigDecimal().toTrimmedString()

fun Float.toTrimmedString(): String = toBigDecimal().toTrimmedString()

fun Int?.orZero(): Int = this ?: 0

fun Long?.orZero(): Long = this ?: 0.toLong()

fun Float?.orZero(): Float = this ?: 0.toFloat()

fun Double?.orZero(): Double = this ?: 0.toDouble()

fun Short?.orZero(): Short = this ?: 0.toShort()

fun Byte?.orZero(): Byte = this ?: 0.toByte()

fun BigDecimal?.orZero(): BigDecimal = this ?: 0.toBigDecimal()

fun BigInteger?.orZero(): BigInteger = this ?: 0.toBigInteger()

inline fun Int?.orElse(defaultValue: () -> Int): Int = this ?: defaultValue()

inline fun Long?.orElse(defaultValue: () -> Long): Long = this ?: defaultValue()

inline fun Float?.orElse(defaultValue: () -> Float): Float = this ?: defaultValue()

inline fun Double?.orElse(defaultValue: () -> Double): Double = this ?: defaultValue()

inline fun Short?.orElse(defaultValue: () -> Short): Short = this ?: defaultValue()

inline fun Byte?.orElse(defaultValue: () -> Byte): Byte = this ?: defaultValue()

inline fun BigDecimal?.orElse(defaultValue: () -> BigDecimal): BigDecimal = this ?: defaultValue()

inline fun BigInteger?.orElse(defaultValue: () -> BigInteger): BigInteger = this ?: defaultValue()