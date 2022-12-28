package com.al10101.android.soleil.uniforms

import com.al10101.android.soleil.data.Vector

data class Uniforms(
    var modelMatrix: FloatArray,
    var viewMatrix: FloatArray,
    var projectionMatrix: FloatArray,
    var cameraPosition: Vector,
    var lightArray: LightArray?,
    var textureIds: IntArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Uniforms

        if (!modelMatrix.contentEquals(other.modelMatrix)) return false
        if (!viewMatrix.contentEquals(other.viewMatrix)) return false
        if (!projectionMatrix.contentEquals(other.projectionMatrix)) return false
        if (!textureIds.contentEquals(other.textureIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelMatrix.contentHashCode()
        result = 31 * result + viewMatrix.contentHashCode()
        result = 31 * result + projectionMatrix.contentHashCode()
        result = 31 * result + textureIds.contentHashCode()
        return result
    }

}