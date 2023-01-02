package com.al10101.android.soleil.uniforms

import com.al10101.android.soleil.data.Vector

class LightArray(lights: List<Light>) {

    val size = lights.size
    val lightPositions = FloatArray(size * 3)
    val lightColors = FloatArray(size * 3)
    val lightSpeculars = FloatArray(size * 3)
    val lightIntensities = FloatArray(size)
    val lightAttenuations = FloatArray(size * 3)
    val lightTypes = IntArray(size)

    init {
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
    }

    fun setPositionAt(i: Int, newPos: Vector) {
        val offset = 3 * i
        lightPositions[0 + offset] = newPos.x
        lightPositions[1 + offset] = newPos.y
        lightPositions[2 + offset] = newPos.z
    }

    fun getPositionAt(i: Int): Vector {
        val offset = 3 * i
        return Vector(
            lightPositions[0 + offset],
            lightPositions[1 + offset],
            lightPositions[2 + offset]
        )
    }

}