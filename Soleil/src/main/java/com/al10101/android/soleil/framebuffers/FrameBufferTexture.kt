package com.al10101.android.soleil.framebuffers

import android.opengl.GLES20.*
import android.util.Log
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.rotation
import com.al10101.android.soleil.models.nativemodels.Quad
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FRAME_BUFFERS_TAG

class FrameBufferTexture(
    program: ShaderProgram,
    width: Int,
    height: Int
): FrameBuffer(program, width, height) {

    private val ndcQuad: Quad = Quad(program, 2f, 2f,)
    private val ndcUniforms: Uniforms = Uniforms.normalizedDeviceCoordinates().apply {
        textureIds = IntArray(1) // We will use 1 texture
    }

    init {
        generateFBO()
    }

    override fun generateFBO() {

        // Configure depth map fbo
        glGenFramebuffers(1, fbo, 0)

        // Create depth texture
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture[0], 0)

        // Create render buffer for depth testing
        glGenRenderbuffers(1, rbo, 0)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo[0])
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        // Attach the render buffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rbo[0])

        // Unbind framebuffer to make sure we are not accidentally rendering to the wrong buffer
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Log.e(FRAME_BUFFERS_TAG, "FRAMEBUFFER: Framebuffer is not complete!")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    override fun useFBO() {

        // Bind so the next gl calls write into this buffer
        glViewport(0, 0, width, height)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

    }

    fun renderQuad() {

        // Since the rendering has been completed by now, we bind the texture to the uniforms
        ndcUniforms.textureIds!![0] = texture[0]

        // Reset state
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, width, height)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)

        ndcQuad.onRender(ndcUniforms)

    }

}