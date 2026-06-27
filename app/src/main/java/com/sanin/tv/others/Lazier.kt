package com.sanin.tv.others

import kotlin.reflect.KFunction

data class Lazier<T>(
    val factory: () -> T,
    val name: String,
    val lClass: KFunction<T>? = null
) {
    val get = lazy { factory() ?: lClass?.call() }
}

fun <T> lazyList(vararg objects: Pair<String, () -> T>): List<Lazier<T>> =
    objects.map { Lazier(it.second, it.first) }
