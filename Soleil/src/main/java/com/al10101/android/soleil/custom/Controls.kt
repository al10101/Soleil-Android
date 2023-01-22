package com.al10101.android.soleil.custom

import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.identity

data class Controls(
    var oldDist: Float = 0f,
    var midTouch: Vector = Vector.zero,
    var globalTranslation: Vector = Vector.zero,
    // For perspective zoom
    var startFov: Float = 0f,
    var currentFov: Float = 0f,
    // For translation zoom, This zoom considers that the center of the camera is focused at Vector.zero
    var startPosZ: Float = 0f,
    var currentPosZ: Float = 0f,
    var previousTouch: Vector = Vector.zero,
    var currentTouch: Vector = Vector.zero
) {

    var dragMatrix: FloatArray = FloatArray(16).apply { identity() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Controls

        if (!dragMatrix.contentEquals(other.dragMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        return dragMatrix.contentHashCode()
    }
}
