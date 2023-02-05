package com.al10101.android.soleil.models

data class Face(
    val a: Short, val b: Short, val c: Short
) {
    constructor(
        a: Int, b: Int, c: Int
    ): this(a.toShort(), b.toShort(), c.toShort())
}
