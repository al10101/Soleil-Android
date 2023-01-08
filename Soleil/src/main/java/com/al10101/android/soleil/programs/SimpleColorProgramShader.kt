package com.al10101.android.soleil.programs

import android.content.Context
import com.al10101.android.soleil.R

class SimpleColorProgramShader(
    context: Context
): ShaderProgram(
    context,
    R.raw.simple_color_vs,
    R.raw.simple_color_fs
)