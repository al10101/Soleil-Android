package com.al10101.android.soleil.uniforms

import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.identity
import com.al10101.android.soleil.extensions.translation

data class Uniforms(
    var modelMatrix: FloatArray,
    var viewMatrix: FloatArray,
    var projectionMatrix: FloatArray,
    var cameraPosition: Vector,
    var lightArray: LightArray? = null,
    var shadowTextureId: Int = 0
) {

    val lightSpaceMatrix = FloatArray(16)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Uniforms

        if (!modelMatrix.contentEquals(other.modelMatrix)) return false
        if (!viewMatrix.contentEquals(other.viewMatrix)) return false
        if (!projectionMatrix.contentEquals(other.projectionMatrix)) return false
        if (!lightSpaceMatrix.contentEquals(other.lightSpaceMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelMatrix.contentHashCode()
        result = 31 * result + viewMatrix.contentHashCode()
        result = 31 * result + projectionMatrix.contentHashCode()
        result = 31 * result + lightSpaceMatrix.contentHashCode()
        return result
    }

    companion object {

        fun normalizedDeviceCoordinates(): Uniforms {
            return Uniforms(
                FloatArray(16).apply { identity() },
                FloatArray(16).apply { identity() },
                FloatArray(16).apply { identity() },
                Vector.zero
            )
        }

    }

}