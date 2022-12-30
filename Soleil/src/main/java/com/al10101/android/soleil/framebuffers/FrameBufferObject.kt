package com.al10101.android.soleil.framebuffers

import android.content.Context
import com.al10101.android.soleil.R
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms

open class FrameBufferObject(
    context: Context,
    fragmentShaderResId: Int = R.raw.simple_texture_fs,
    val width: Int,
    val height: Int
) {

    val frameBufferProgram = ShaderProgram(context,
        R.raw.simple_texture_vs,
        fragmentShaderResId
    )

    val fbo = IntArray(1)
    val rbo = IntArray(1)
    val texture = IntArray(1)

    open fun onGenerate() {
        // Override to initialize
    }

    open fun onRender(models: List<Model>, uniforms: Uniforms) {
        // Override to set fbo
    }

}