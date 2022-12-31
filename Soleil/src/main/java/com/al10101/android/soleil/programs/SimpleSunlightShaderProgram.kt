package com.al10101.android.soleil.programs

import android.content.Context
import com.al10101.android.soleil.R

class SimpleSunlightShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.simple_sunlight_vs,
    R.raw.simple_sunlight_fs
)