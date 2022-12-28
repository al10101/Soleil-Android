package com.al10101.android.soleil.uniforms

import android.opengl.Matrix.*
import com.al10101.android.soleil.data.Rectangle
import com.al10101.android.soleil.data.Vector

class Camera @JvmOverloads constructor(
    var position: Vector = Vector(0f, 0f, 5f),
    var center: Vector = Vector.zero,
    var up: Vector = Vector(0f, 1f, 0f),
    var aspect: Float = 1f,
    var fovy: Float = 45f,
    var near: Float = 0.01f,
    var far: Float = 50f
) {

    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val viewProjectionMatrix = FloatArray(16)

    fun setViewMatrix() {
        setLookAtM(
            viewMatrix, 0,
            position.x, position.y, position.z,
            center.x, center.y, center.z,
            up.x, up.y, up.z
        )
    }

    fun setOrthographicProjectionMatrix(rect: Rectangle) {
        orthoM(
            projectionMatrix, 0,
            rect.left, rect.right, rect.bottom, rect.top,
            near, far
        )
    }

    fun setPerspectiveProjectionMatrix() {
        perspectiveM(projectionMatrix, 0, fovy, aspect, near, far)
    }

    fun setVPMatrix() {
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

}