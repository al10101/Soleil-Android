package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.extensions.readTextFileFromResource
import com.al10101.android.soleil.utils.ShaderUtils

const val U_PROJECTION_MATRIX = "u_ProjectionMatrix"
const val U_VIEW_MATRIX = "u_ViewMatrix"
const val U_MODEL_MATRIX = "u_ModelMatrix"

const val U_TEXTURE_UNIT = "u_TextureUnit"

const val U_CAMERA_POSITION = "u_CameraPosition"

const val U_LIGHT_POSITION = "u_LightPosition"
const val U_LIGHT_COLOR = "u_LightColor"
const val U_LIGHT_SPECULAR = "u_LightSpecular"
const val U_LIGHT_INTENSITY = "u_LightIntensity"
const val U_LIGHT_ATTENUATION = "u_LightAttenuation"
const val U_LIGHT_TYPE = "u_LightType"

const val A_POSITION = "a_Position"
const val A_COLOR = "a_Color"
const val A_NORMAL = "a_Normal"
const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

open class ShaderProgram(
    context: Context,
    vertexShaderResId: Int,
    fragmentShaderResId: Int
) {

    // Uniform locations
    private val uProjectionMatrixLocation by lazy {
        glGetUniformLocation(program, U_PROJECTION_MATRIX)
    }
    private val uViewMatrixLocation by lazy {
        glGetUniformLocation(program, U_VIEW_MATRIX)
    }
    private val uModelMatrixLocation by lazy {
        glGetUniformLocation(program, U_MODEL_MATRIX)
    }

    private val uCameraPosition by lazy {
        glGetUniformLocation(program, U_CAMERA_POSITION)
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

    private val uTextureUnitLocation by lazy {
        glGetUniformLocation(program, U_TEXTURE_UNIT)
    }

    // Attribute locations
    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }
    val aColorLocation by lazy {
        glGetAttribLocation(program, A_COLOR)
    }
    val aNormalLocation by lazy {
        glGetAttribLocation(program, A_NORMAL)
    }
    val aTextureCoordinatesLocation by lazy {
        glGetAttribLocation(program, A_TEXTURE_COORDINATES)
    }

    val program by lazy {
        ShaderUtils.buildProgram(
            context.readTextFileFromResource(vertexShaderResId),
            context.readTextFileFromResource(fragmentShaderResId)
        )
    }

    fun useProgram() {
        glUseProgram(program)
    }

    open fun setUniforms(uniforms: Uniforms) {
        // Set up the matrices
        glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, uniforms.projectionMatrix, 0)
        glUniformMatrix4fv(uViewMatrixLocation, 1, false, uniforms.viewMatrix, 0)
        glUniformMatrix4fv(uModelMatrixLocation, 1, false, uniforms.modelMatrix, 0)
        // Set up the camera position
        glUniform3fv(uCameraPosition, 1, uniforms.cameraPosition.toFloatArray(), 0)
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
        // Set up ONLY THE FIRST TEXTURE
        uniforms.textureIds?.let {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, it[0])
            glUniform1i(uTextureUnitLocation, 0)
        }
    }

}