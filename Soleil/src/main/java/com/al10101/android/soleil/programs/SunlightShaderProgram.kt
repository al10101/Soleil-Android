package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

const val U_LIGHT_SPACE_MATRIX = "u_LightSpaceMatrix"

const val U_LIGHT_POSITION = "u_LightPosition"
const val U_LIGHT_COLOR = "u_LightColor"
const val U_LIGHT_SPECULAR = "u_LightSpecular"
const val U_LIGHT_INTENSITY = "u_LightIntensity"
const val U_LIGHT_ATTENUATION = "u_LightAttenuation"
const val U_LIGHT_TYPE = "u_LightType"

const val U_SHADOW_TEXTURE_UNIT = "u_ShadowTextureUnit"

class SunlightShaderProgram(
    context: Context
): ShaderProgram(
    context,
    R.raw.sunlight_vs,
    R.raw.sunlight_fs
) {

    private val uLightSpaceMatrixLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_SPACE_MATRIX)
    }

    private val uLightPositionLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_POSITION)
    }
    private val uLightColorLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_COLOR)
    }
    private val uLightSpecularLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_SPECULAR)
    }
    private val uLightIntensityLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_INTENSITY)
    }
    private val uLightAttenuationLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_ATTENUATION)
    }
    private val uLightTypeLocation by lazy {
        glGetUniformLocation(program, U_LIGHT_TYPE)
    }

    private val uShadowTextureUnitLocation by lazy {
        glGetUniformLocation(program, U_SHADOW_TEXTURE_UNIT)
    }

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set up the matrix that represents the camera from Sunlight perspective
        glUniformMatrix4fv(uLightSpaceMatrixLocation, 1, false, uniforms.lightSpaceMatrix, 0)
        // Set up the lights
        uniforms.lightArray?.let {
            val nLights = it.size // The shader will only use 1 light anyway
            glUniform3fv(uLightPositionLocation, nLights, it.lightPositions, 0)
            glUniform3fv(uLightColorLocation, nLights, it.lightColors, 0)
            glUniform3fv(uLightSpecularLocation, nLights, it.lightSpeculars, 0)
            glUniform1fv(uLightIntensityLocation, nLights, it.lightIntensities, 0)
            glUniform3fv(uLightAttenuationLocation, nLights, it.lightAttenuations, 0)
            glUniform1iv(uLightTypeLocation, nLights, it.lightTypes, 0)
        }
        // Set up the shadow texture. It should be bound with correct texture by now
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, uniforms.shadowTextureId)
        glUniform1i(uShadowTextureUnitLocation, 0)
    }

}