package com.al10101.android.soleil.data

import android.graphics.Color

class RGB(
    var r: Float,
    var g: Float,
    var b: Float
) {

    fun toFloatArray() = floatArrayOf(r, g, b)

    fun scale(rate: Float) = RGB(r * rate, g * rate, b * rate)

    fun weightedAverage(weight: Float, other: RGB, otherWeight: Float): RGB {
        val sum = weight + otherWeight
        val newA = scale(weight)
        val newB = other.scale(otherWeight)
        return RGB(
            newA.r + newB.r,
            newA.g + newB.g,
            newA.b + newB.b
        ).scale(1f / sum)
    }

    companion object {
        val white = RGB(1f, 1f, 1f)
        val black = RGB(0f, 0f, 0f)
        val red = RGB(1f, 0f, 0f)
        val green = RGB(0f, 1f, 0f)
        val eyeSensibleGreen = RGB(0.2126f, 0.7152f, 0.0722f)
        val blue = RGB(0f, 0f, 1f)
        val yellow = RGB(1f, 1f, 0f)
        val magenta = RGB(1f, 0f, 1f)
        val cyan = RGB(0f, 1f, 1f)
        fun grayScale(g: Float) = RGB(g, g, g)
        fun fromHex(hexHash: String): RGB {
            val color = Color.parseColor(hexHash)
            return RGB(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
            )
        }
    }

}