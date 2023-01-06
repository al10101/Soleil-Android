package com.al10101.android.soleil.programs

import android.content.Context
import com.al10101.android.soleil.R

class LambertianShaderProgram(
    context: Context
): LightingShaderProgram(
    context,
    R.raw.lambertian_vs,
    R.raw.lambertian_fs
)