package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

private const val U_GAMMA = "u_Gamma"

class GammaCorrectionShaderProgram(
    context: Context,
    var gamma: Float = 2.2f
): ShaderProgram(
    context,
    R.raw.simple_texture_vs,
    R.raw.gamma_correction_fs
) {

    private val uGammaLocation by lazy {
        glGetUniformLocation(program, U_GAMMA)
    }

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set gamma factor
        glUniform1f(uGammaLocation, gamma)
    }

}