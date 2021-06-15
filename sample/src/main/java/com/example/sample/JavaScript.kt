package com.example.sample

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

fun Any.toJs(scope: Scriptable): Any = Context.javaToJS(this, scope)

@Suppress("UNCHECKED_CAST")
fun <T> Any.toJava(classOfT: Class<T>): T = Context.jsToJava(this, classOfT) as T

inline fun <reified T> Any.toJava(): T = toJava(T::class.java)

inline fun <reified T> Context.evaluateString(
    scope: Scriptable,
    source: String,
    sourceName: String,
    lineno: Int = 0,
    securityDomain: Any? = null,
): T = evaluateString(scope, source, sourceName, lineno, securityDomain).toJava()

inline fun <reified T> Context.evaluateString(scope: Scriptable, source: String) {}