package com.al10101.android.soleil.programs

import android.content.Context
import com.al10101.android.soleil.R

class SimpleTextureShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.simple_texture_vs,
    R.raw.simple_texture_fs
)