package com.al10101.android.soleil.framebuffers

import android.opengl.GLES20.*
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms

open class FrameBuffer(
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

    fun unbindFrameBuffer() {
        glViewport(0, 0, screenWidth, screenHeight)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)
    }

}