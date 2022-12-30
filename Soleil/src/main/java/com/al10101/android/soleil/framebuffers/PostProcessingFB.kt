package com.al10101.android.soleil.framebuffers

import android.content.Context
import android.opengl.GLES20.*
import android.util.Log
import com.al10101.android.soleil.R
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Quad
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FRAME_BUFFERS_TAG
import com.al10101.android.soleil.utils.MODELS_TAG

class PostProcessingFB(
    postProgram: ShaderProgram,
    private val screenWidth: Int,
    private val screenHeight: Int
): FrameBuffer {

    private val fbo = IntArray(1)
    private val rbo = IntArray(1)
    private val texture = IntArray(1)

    private val ndcQuad: Quad = Quad(postProgram, 2f, 2f)
    private val ndcUniforms: Uniforms = Uniforms.normalizedDeviceCoordinates().apply {
        textureIds = IntArray(1) // We will use 1 texture
    }

    init {
        onGenerate()
    }

    override fun onGenerate() {

        // Configure depth map FBO
        glGenFramebuffers(1, fbo, 0)

        // Create depth texture
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture[0], 0)

        // Create render buffer for depth testing
        glGenRenderbuffers(1, rbo, 0)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo[0])
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, screenWidth, screenHeight)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        // Attach the render buffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rbo[0])

        // Unbind framebuffer to make sure we are not accidentally rendering to the wrong buffer
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Log.e(MODELS_TAG, "FRAMEBUFFER: Framebuffer is not complete!")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    override fun onRender(models: List<Model>, uniforms: Uniforms): Uniforms {

        // Bind so the next gl calls write into this buffer
        glViewport(0, 0, screenWidth, screenHeight)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

        // Render all models
        models.forEach { it.onRender(uniforms) }

        // Now that the rendering has been completed, we bind the texture to the uniforms
        ndcUniforms.textureIds!![0] = texture[0]

        // Reset state for the final post-processing rendering
        glViewport(0, 0, screenWidth, screenHeight)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClear(GL_COLOR_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)

        ndcQuad.onRender(ndcUniforms)

        return uniforms

    }

}