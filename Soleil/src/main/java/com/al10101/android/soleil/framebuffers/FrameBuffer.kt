package com.al10101.android.soleil.framebuffers

import com.al10101.android.soleil.programs.ShaderProgram

open class FrameBuffer(
    val program: ShaderProgram,
    val width: Int,
    val height: Int
) {

    val fbo = IntArray(1)
    val rbo = IntArray(1)
    val texture = IntArray(1)

    open fun generateFBO() {
        // Override to initialize
    }

    open fun useFBO() {
        // Override to set fbo
    }

}