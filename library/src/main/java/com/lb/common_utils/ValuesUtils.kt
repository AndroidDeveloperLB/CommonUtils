package com.lb.common_utils

fun Long.orIfZero(alternativeValue: Long) = if (this == 0L) alternativeValue else this

fun Int.inRange(minValue: Int, maxValue: Int): Boolean = this in minValue..maxValue

inline fun <C> C.ifNotBlank(defaultValue: (C) -> Unit) where C : CharSequence {
    if (isNotBlank())
        defaultValue(this)
}

inline fun <C> C.ifNotEmpty(defaultValue: (C) -> Unit) where C : Collection<*> {
    if (isNotEmpty())
        defaultValue(this)
}

object ValuesUtils {
}
