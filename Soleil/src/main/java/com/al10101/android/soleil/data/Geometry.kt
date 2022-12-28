package com.al10101.android.soleil.data

import android.util.FloatMath
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class Vector(
    var x: Float,
    var y: Float,
    var z: Float
) {

    fun toFloatArray() = floatArrayOf(x, y, z)

    fun add(other: Vector) = Vector(
        x + other.x,
        y + other.y,
        z + other.z
    )
    fun add(scalar: Float) = Vector(
        x + scalar,
        y + scalar,
        z + scalar
    )

    fun sub(other: Vector) = Vector(
        x - other.x,
        y - other.y,
        z - other.z
    )
    fun sub(scalar: Float) = Vector(
        x - scalar,
        y - scalar,
        z - scalar
    )

    fun mul(other: Vector) = Vector(
        x * other.x,
        y * other.y,
        z * other.z
    )
    fun mul(scalar: Float) = Vector(
        x * scalar,
        y * scalar,
        z * scalar
    )

    fun div(scalar: Float) = Vector(
        x / scalar,
        y / scalar,
        z / scalar
    )

    fun length() = sqrt(
        x*x + y*y + z*z
    )

    fun dot(other: Vector) =
        x*other.x + y*other.y + z*other.z

    fun cross(other: Vector) = Vector(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x,
    )

    fun normalize(): Vector {
        val length = length()
        return Vector(x / length, y / length, z / length)
    }

    fun max(other: Vector) = Vector(
        max(x, other.x),
        max(y, other.y),
        max(z, other.z)
    )
    fun max() = max( max(x, y), z )

    companion object {
        val zero = Vector(0f, 0f, 0f)
        val one = Vector(1f, 1f, 1f)
        fun random(random: Random) = Vector(
            random.nextFloat(),
            random.nextFloat(),
            random.nextFloat(),
        )
    }

}

data class Rectangle(
    val left: Float,
    val right: Float,
    val bottom: Float,
    val top: Float
)
