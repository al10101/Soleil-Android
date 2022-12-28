package com.al10101.android.soleil.models

import com.al10101.android.soleil.data.Vector

class LightArray(
    val size: Int,
    val lightPositions: FloatArray,
    val lightColors: FloatArray,
    val lightSpeculars: FloatArray,
    val lightIntensities: FloatArray,
    val lightAttenuations: FloatArray,
    val lightTypes: IntArray
) {

    fun changePositionAt(i: Int, newPos: Vector) {
        val offset = 3 * i
        lightPositions[0 + offset] = newPos.x
        lightPositions[1 + offset] = newPos.y
        lightPositions[2 + offset] = newPos.z
    }

    companion object {

        fun unrollLights(lights: List<Light>): LightArray {
            val size = lights.size
            val lightPositions = FloatArray(size * 3)
            val lightColors = FloatArray(size * 3)
            val lightSpeculars = FloatArray(size * 3)
            val lightIntensities = FloatArray(size)
            val lightAttenuations = FloatArray(size * 3)
            val lightTypes = IntArray(size)
            lights.forEachIndexed { i, light ->
                val offset = i * 3
                // FloatArray
                lightPositions[0 + offset] = light.position.x
                lightPositions[1 + offset] = light.position.y
                lightPositions[2 + offset] = light.position.z
                // FloatArray
                lightColors[0 + offset] = light.color.r
                lightColors[1 + offset] = light.color.g
                lightColors[2 + offset] = light.color.b
                // FloatArray
                lightSpeculars[0 + offset] = light.specular.r
                lightSpeculars[1 + offset] = light.specular.g
                lightSpeculars[2 + offset] = light.specular.b
                // Float
                lightIntensities[i] = light.intensity
                // FloatArray
                lightAttenuations[0 + offset] = light.attenuation.x
                lightAttenuations[1 + offset] = light.attenuation.y
                lightAttenuations[2 + offset] = light.attenuation.z
                // Int
                lightTypes[i] = light.type
            }
            return LightArray(
                size,
                lightPositions,
                lightColors,
                lightSpeculars,
                lightIntensities,
                lightAttenuations,
                lightTypes
            )
        }

    }

}