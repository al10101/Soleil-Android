package com.al10101.android.soleil.uniforms

import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.utils.LightTypes

data class Light(
    var position: Vector,
    var color: RGB,
    var specular: RGB,
    var intensity: Float,
    var attenuation: Vector,
    var type: Int
) {

    companion object {
        fun buildDefaultLight() = Light(
            position = Vector.one,
            color = RGB.white,
            specular = RGB.grayScale(0.6f),
            intensity = 1f,
            attenuation = Vector(1f, 0f, 0f),
            type = LightTypes.SUNLIGHT
        )
    }

}
