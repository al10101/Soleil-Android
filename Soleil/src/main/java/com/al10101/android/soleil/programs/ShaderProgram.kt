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

    private val uTextureUnitLocations by lazy {
        // Declare up to ten textures per shader
        val maxTextures = 10
        val textureLocations = IntArray(maxTextures)
        for (i in 0 until maxTextures) {
            textureLocations[i] = glGetUniformLocation(program, U_TEXTURE_UNIT + "$i")
        }
        textureLocations
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

    open fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        // Set up the matrices
        glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, uniforms.projectionMatrix, 0)
        glUniformMatrix4fv(uViewMatrixLocation, 1, false, uniforms.viewMatrix, 0)
        glUniformMatrix4fv(uModelMatrixLocation, 1, false, uniforms.modelMatrix, 0)
        // Set up the camera position
        glUniform3fv(uCameraPosition, 1, uniforms.cameraPosition.toFloatArray(), 0)
        // Set up all the textures
        glBindTexture(GL_TEXTURE_2D, 0)
        textureIds.forEachIndexed { i, it ->
            glActiveTexture(GL_TEXTURE1 + i) // excluding TEXTURE0, it is reserved for the shadow
            glBindTexture(GL_TEXTURE_2D, it)
            glUniform1i(uTextureUnitLocations[i], 1+i)
        }
    }

}