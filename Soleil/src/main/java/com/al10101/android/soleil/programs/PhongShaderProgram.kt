package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

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

    override fun setUniforms(uniforms: Uniforms, textureIds: List<Int>) {
        super.setUniforms(uniforms, textureIds)
        // Set material shininess
        glUniform1f(uMaterialShininessLocation, materialShininess)
    }

}