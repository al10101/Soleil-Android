package com.al10101.android.soleil.framebuffers

import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms

open class FrameBuffer(
    val frameBufferProgram: ShaderProgram,
    val fbWidth: Int,
    val fbHeight: Int,
    val screenWidth: Int,
    val screenHeight: Int,
) {

    protected val fbo = IntArray(1)
    protected val rbo = IntArray(1)
    protected val texture = IntArray(1)

    open fun onGenerate() {
        // Override to initialize fbo
    }

    open fun onRender(models: List<Model>, uniforms: Uniforms) {
        // Override to render models and link to fbo
    }

}