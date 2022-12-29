package com.al10101.android.soleil.utils

import android.hardware.lights.Light

// OpenGL constants
const val OPENGL_ZERO = 0

// Default Tags
const val SHADER_UTILS_TAG = "SoleilShaderUtilsTag"
const val FRAME_RATE_TAG = "SoleilFrameRateTag"
const val CONTEXT_EXT_TAG = "SoleilContextExtTag"
const val MODELS_TAG  = "SoleilModelsTag"

// Counters
const val BYTES_PER_FLOAT = 4

// Units
const val NANOSECONDS = 1_000_000_000f
const val RADIANS_TO_DEGREES = 57.2958f
const val DEGREES_TO_RADIANS =  0.0174f

// Types of light. They must be primitives because the same value
// must be used for the corresponding shader programs
object LightTypes {
    const val SUNLIGHT = 0
    const val AMBIENT_LIGHT = 1
    const val POINT_LIGHT = 2
    const val SPOTLIGHT = 3
}
