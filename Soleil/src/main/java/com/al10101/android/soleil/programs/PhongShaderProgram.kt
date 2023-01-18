package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

private const val U_CAMERA_POSITION = "u_CameraPosition"
private const val U_MATERIAL_SHININESS = "u_MaterialShininess"

open class PhongShaderProgram(
    context: Context,
    var materialShininess: Float,
    vertexShaderResId: Int = R.raw.phong_vs,
    fragmentShaderResId: Int = R.raw.phong_fs
): LightingShaderProgram(context, vertexShaderResId, fragmentShaderResId) {

    private val uMaterialShininessLocation by lazy {
        glGetUniformLocation(program, U_MATERIAL_SHININESS)
    }

    private val uCameraPosition by lazy {
        glGetUniformLocation(program, U_CAMERA_POSITION)
    }

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set up the camera position
        glUniform3fv(uCameraPosition, 1, uniforms.cameraPosition.toFloatArray(), 0)
        // Set material shininess
        glUniform1f(uMaterialShininessLocation, materialShininess)
    }

}