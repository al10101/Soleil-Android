package com.al10101.android.soleil.extensions

import android.opengl.Matrix.*
import com.al10101.android.soleil.data.Vector

// 4x4 matrix
fun FloatArray.column(j: Int): FloatArray {
    val offset = j * 4
    return floatArrayOf(
        this[offset + 0],
        this[offset + 1],
        this[offset + 2],
        this[offset + 3]
    )
}

// Extract translation
fun FloatArray.getTranslation(): Vector {
    return column(3).toVector()
}

// Extract scaling
fun FloatArray.getScaling(): Vector {
    val sx = column(0).toVector().length()
    val sy = column(1).toVector().length()
    val sz = column(2).toVector().length()
    return Vector(sx, sy, sz)
}

// 4x4 matrix
fun FloatArray.row(i: Int) = floatArrayOf(
    this[0 + i],
    this[4 + i],
    this[8 + i],
    this[12+ i]
)

// 4 vector
fun FloatArray.xyzw() = floatArrayOf(
    this[0],
    this[1],
    this[2],
    this[3]
)

// 4 vector
fun FloatArray.x() = this[0]
fun FloatArray.y() = this[1]
fun FloatArray.z() = this[2]
fun FloatArray.w() = this[3]

// 3 vector
fun FloatArray.xy() = floatArrayOf(this[0], this[1])
fun FloatArray.yz() = floatArrayOf(this[1], this[2])
fun FloatArray.xz() = floatArrayOf(this[0], this[2])

// 3 vector
fun FloatArray.toVector() = Vector(
    this[0],
    this[1],
    this[2]
)

fun FloatArray.identity() {
    setIdentityM(this, 0)
}

fun FloatArray.translation(translate: Vector) {
    identity()
    translateM(this, 0, translate.x, translate.y, translate.z)
}

fun FloatArray.rotation(rotate: Vector) {
    identity()
    rotateM(this, 0, rotate.x, 1f, 0f, 0f)
    rotateM(this, 0, rotate.y, 0f, 1f, 0f)
    rotateM(this, 0, rotate.z, 0f, 0f, 1f)
}

fun FloatArray.scaling(scale: Vector) {
    identity()
    scaleM(this, 0, scale.x, scale.y, scale.z)
}