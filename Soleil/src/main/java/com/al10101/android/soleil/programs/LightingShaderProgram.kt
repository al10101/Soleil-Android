package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.uniforms.Uniforms

private const val U_LIGHT_POSITION = "u_LightPosition"
private const val U_LIGHT_COLOR = "u_LightColor"
private const val U_LIGHT_SPECULAR = "u_LightSpecular"
private const val U_LIGHT_INTENSITY = "u_LightIntensity"
private const val U_LIGHT_ATTENUATION = "u_LightAttenuation"
private const val U_LIGHT_TYPE = "u_LightType"

open class LightingShaderProgram(
    context: Context,
    vertexShaderResId: Int,
    fragmentShaderResId: Int
): ShaderProgram(context, vertexShaderResId, fragmentShaderResId) {

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

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set up the lights
        uniforms.lightArray?.let {
            val nLights = it.size
            glUniform3fv(uLightPositionLocation, nLights, it.lightPositions, 0)
            glUniform3fv(uLightColorLocation, nLights, it.lightColors, 0)
            glUniform3fv(uLightSpecularLocation, nLights, it.lightSpeculars, 0)
            glUniform1fv(uLightIntensityLocation, nLights, it.lightIntensities, 0)
            glUniform3fv(uLightAttenuationLocation, nLights, it.lightAttenuations, 0)
            glUniform1iv(uLightTypeLocation, nLights, it.lightTypes, 0)
        }
    }
    
}