package com.al10101.android.soleil.uniforms

import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.utils.LightTypes

data class Light(
    var position: Vector = Vector.one,
    var color: RGB = RGB.white,
    var specular: RGB = RGB.grayScale(0.6f),
    var intensity: Float = 1f,
    var attenuation: Vector = Vector(1f, 0f, 0f),
    var type: Int = LightTypes.SUNLIGHT
)
