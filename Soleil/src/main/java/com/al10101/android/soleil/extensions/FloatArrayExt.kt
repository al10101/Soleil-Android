package com.al10101.android.soleil.extensions

import android.opengl.Matrix
import android.opengl.Matrix.*
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.utils.RADIANS_TO_DEGREES
import kotlin.math.acos

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

// 4 vector
fun FloatArray.divideByW() {
    this[0] /= this[3]
    this[1] /= this[3]
    this[2] /= this[3]
}

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

fun FloatArray.rotateX(angle: Float) {
    identity()
    rotateM(this, 0, angle, 1f, 0f, 0f)
}
fun FloatArray.rotateY(angle: Float) {
    identity()
    rotateM(this, 0, angle, 0f, 1f, 0f)
}
fun FloatArray.rotateZ(angle: Float) {
    identity()
    rotateM(this, 0, angle, 0f, 0f, 1f)
}

fun FloatArray.rotation(q: Quaternion) {
    // Not as straight forward as the other transformations. We need to imagine a reference
    // vector and rotate until that reference vector points to the direction from the input.
    // We then compute the angle and the vector from which we'll perform the rotation.
    // The cross product would be zero if both vectors are parallel or anti-parallel, the sign
    // of cosTheta tells us the direction
    val cross = q.ref.cross(q.dir)
    val cosTheta = q.ref.dot(q.dir) / (q.ref.length() * q.dir.length())
    if (cross.length() == 0f) {
        identity()
        if (cosTheta > 0f) {
            // Do nothing, since that is the reference
            return
        } else if (cosTheta < 0f) {
            // Rotate to point the other side
            val oUnit = q.ref.normalize()
            rotateM(this, 0, 180f, oUnit.z, oUnit.x, oUnit.y)
            return
        }
    }
    // Now that the danger is gone, we compute unitary vector from which we'll perform the
    // rotation
    val crossUnit = cross.normalize()
    // Angle of rotation in degrees because the Matrix.rotateM() method uses degrees
    val theta = acos(cosTheta) * RADIANS_TO_DEGREES
    identity()
    rotateM(this, 0, theta, crossUnit.x, crossUnit.y, crossUnit.z)
}

fun FloatArray.scaling(scale: Vector) {
    identity()
    scaleM(this, 0, scale.x, scale.y, scale.z)
}

// From a 4x4 matrix, get the information corresponding to the position
fun FloatArray.extractTranslation(): Vector {
    return this.column(3).toVector()
}

// From a 4x4 matrix, get the information corresponding to the scale
fun FloatArray.extractScaling(): Vector {
    val sx = this.column(0).toVector().length()
    val sy = this.column(1).toVector().length()
    val sz = this.column(2).toVector().length()
    return Vector(sx, sy, sz)
}

// From a 4x4 matrix, reduce to get the rotation matrix
fun FloatArray.reduceToRotation() {
    val s = extractScaling()
    this[0] /= s.x ; this[4] /= s.y ; this[8]  /= s.z ; this[12] = 0f
    this[1] /= s.x ; this[5] /= s.y ; this[9]  /= s.z ; this[13] = 0f
    this[2] /= s.x ; this[6] /= s.y ; this[10] /= s.z ; this[14] = 0f
    this[3]  =  0f ; this[6]  =  0f ; this[11]  =  0f ; this[15] = 1f
}

fun FloatArray.toModelMatrix(position: Vector, rotation: Quaternion, scale: Vector) {
    val translateMatrix = FloatArray(16).apply { translation(position) }
    val rotateMatrix = FloatArray(16).apply { rotation(rotation) }
    val scaleMatrix = FloatArray(16).apply { scaling(scale) }

    val scalingRotation = FloatArray(16)
    multiplyMM(scalingRotation, 0, rotateMatrix, 0, scaleMatrix, 0)
    multiplyMM(this, 0, translateMatrix, 0, scalingRotation, 0)
}

// Vector 4 to another Vector 4
fun FloatArray.transform(modelMatrix: FloatArray, temp: FloatArray) {
    multiplyMV(temp, 0, modelMatrix, 0, this, 0)
    this[0] = temp[0]
    this[1] = temp[1]
    this[2] = temp[2]
    this[3] = temp[3]
}
