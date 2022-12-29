package com.al10101.android.soleil.programs

import android.content.Context
import com.al10101.android.soleil.R

class SimpleLightShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.simple_lighting_vs,
    R.raw.simple_lighting_fs
)