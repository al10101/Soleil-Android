package com.al10101.android.soleil.data

import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

data class Vector(
    var x: Float,
    var y: Float,
    var z: Float
) {

    fun toFloatArray() = floatArrayOf(x, y, z)

    fun negative() = Vector(-x, -y, -z)

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

    fun pointWise(other: Vector) = Vector(
        x * other.x,
        y * other.y,
        z * other.z
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
        val unitX = Vector(1f, 0f, 0f)
        val unitY = Vector(0f, 1f, 0f)
        val unitZ = Vector(0f, 0f, 1f)
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

data class Quaternion(
    val ref: Vector, // Reference vector
    val dir: Vector  // Direction to which we want the original reference to point
) {
    companion object {
        val upY = Quaternion(
            Vector.unitY,
            Vector.unitY
        )
    }
}

data class Ray(
    val origin: Vector,
    val direction: Vector
) {
    fun distanceBetween(vector: Vector): Float {
        val p1ToOrigin = vector.sub(origin)
        val p2ToOrigin = vector.sub(origin.add(direction))
        // The length of the cross product gives the area of an imaginary
        // parallelogram having the two vectors as sides
        val areaOfTriangleTimesTwo = p1ToOrigin.cross(p2ToOrigin).length()
        val lengthOfBase = direction.length()
        // The area of a triangle is also equal to (base * height) / 2. In
        // other words, the height is equal to (area * 2) / base. The height
        // of this triangle is the distance from the point to the ray
        return areaOfTriangleTimesTwo / lengthOfBase
    }
    // This also treats rays as if they were infinite. It will return a
    // point full of NaNs if there is no intersection point
    fun intersectionPointWith(plane: Plane): Vector {
        val rayToPlaneVector = plane.position.sub(origin)
        val scaleFactor = rayToPlaneVector.dot(plane.normal) /
                direction.dot(plane.normal)
        return origin.add(direction.mul(scaleFactor))
    }
}

data class Plane(
    val position: Vector,
    val normal: Vector
)
