@file:Suppress("unused")

package com.easy.kotlins.helper

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