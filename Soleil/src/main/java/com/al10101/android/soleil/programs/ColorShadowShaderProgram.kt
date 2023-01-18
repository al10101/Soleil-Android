package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

private const val U_LIGHT_SPACE_MATRIX = "u_LightSpaceMatrix"
private const val U_SHADOW_TEXTURE_UNIT = "u_ShadowTextureUnit"

class ColorShadowShaderProgram(
    context: Context
): LightingShaderProgram(
    context,
    R.raw.color_shadow_vs,
    R.raw.color_shadow_fs
) {

    private val uLightSpaceMatrixLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_SPACE_MATRIX)
    }

    private val uShadowTextureUnitLocation by lazy {
        glGetUniformLocation(program, U_SHADOW_TEXTURE_UNIT)
    }

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set up the matrix that represents the camera from Sunlight perspective
        glUniformMatrix4fv(uLightSpaceMatrixLocation, 1, false, uniforms.lightSpaceMatrix, 0)
        // Set up the shadow texture. It should be bound with correct texture by now
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, uniforms.shadowTextureId)
        glUniform1i(uShadowTextureUnitLocation, 0)
    }

}