package com.nice.sqlite.core

import com.nice.sqlite.core.dml.Projection

interface Predicate

/**
 * Not expression
 */
class NotExpression(val param: Predicate) : Predicate

fun not(predicate: Predicate): NotExpression {
    return NotExpression(predicate)
}

/**
 * And expression
 */
class AndExpression(val left: Predicate, val right: Predicate) : Predicate

infix fun Predicate.and(predicate: Predicate): AndExpression {
    return AndExpression(this, predicate)
}

/**
 * Or expression
 */
class OrExpression(val left: Predicate, val right: Predicate) : Predicate

infix fun Predicate.or(predicate: Predicate): OrExpression {
    return OrExpression(this, predicate)
}

/**
 * Equals expression
 */
class EqExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.eq(value: Projection.Column): EqExpression {
    return EqExpression(this, value)
}

infix fun Projection.Column.eq(value: String?): EqExpression {
    return EqExpression(this, value)
}

infix fun Projection.Column.eq(value: Number): EqExpression {
    return EqExpression(this, value)
}

infix fun Projection.Column.eq(value: Boolean): EqExpression {
    return EqExpression(this, value)
}

/**
 * Not equals expression
 */
class NeExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.ne(value: Projection.Column): NeExpression {
    return NeExpression(this, value)
}

infix fun Projection.Column.ne(value: String?): NeExpression {
    return NeExpression(this, value)
}

infix fun Projection.Column.ne(value: Number): NeExpression {
    return NeExpression(this, value)
}

infix fun Projection.Column.ne(value: Boolean): NeExpression {
    return NeExpression(this, value)
}

/**
 * Less than expression
 */
class LtExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.lt(value: Projection.Column): LtExpression {
    return LtExpression(this, value)
}

infix fun Projection.Column.lt(value: String?): LtExpression {
    return LtExpression(this, value)
}

infix fun Projection.Column.lt(value: Number): LtExpression {
    return LtExpression(this, value)
}

/**
 * Less than or equal expression
 */
class LteExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.lte(value: Projection.Column): LteExpression {
    return LteExpression(this, value)
}

infix fun Projection.Column.lte(value: String?): LteExpression {
    return LteExpression(this, value)
}

infix fun Projection.Column.lte(value: Number): LteExpression {
    return LteExpression(this, value)
}

/**
 * Greater than expression
 */
class GtExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.gt(value: Projection.Column): GtExpression {
    return GtExpression(this, value)
}

infix fun Projection.Column.gt(value: String?): GtExpression {
    return GtExpression(this, value)
}

infix fun Projection.Column.gt(value: Number): GtExpression {
    return GtExpression(this, value)
}

/**
 * Greater than or equal expression
 */
class GteExpression(val left: Projection.Column, val right: Any?) : Predicate

infix fun Projection.Column.gte(value: Projection.Column): GteExpression {
    return GteExpression(this, value)
}

infix fun Projection.Column.gte(value: String?): GteExpression {
    return GteExpression(this, value)
}

infix fun Projection.Column.gte(value: Number): GteExpression {
    return GteExpression(this, value)
}