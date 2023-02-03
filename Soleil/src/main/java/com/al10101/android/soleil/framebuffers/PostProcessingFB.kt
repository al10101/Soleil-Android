package com.al10101.android.soleil.framebuffers

import android.opengl.GLES20.*
import android.util.Log
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Quad
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FRAME_BUFFERS_TAG

open class PostProcessingFB(
    shaderProgram: ShaderProgram,
    fbWidth: Int,
    fbHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
): FrameBuffer(shaderProgram, fbWidth, fbHeight, screenWidth, screenHeight) {

    val ndcUniforms: Uniforms = Uniforms.normalizedDeviceCoordinates()
    private val ndcQuad: Quad = Quad(2f, 2f,
        // Only to use the empty space, because the rendering occurs with the super.shaderProgram
        program = shaderProgram,
        // Empty textureId to initialize texture list inside model
        textureId = 0
    )

    init {
        onGenerate()
    }

    override fun onGenerate() {

        // Configure depth map FBO
        glGenFramebuffers(1, fbo, 0)

        // Create depth texture
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, fbWidth, fbHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture[0], 0)

        // Create render buffer for depth testing
        glGenRenderbuffers(1, rbo, 0)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo[0])
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, fbWidth, fbHeight)
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        // Attach the render buffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rbo[0])

        // Unbind framebuffer to make sure we are not accidentally rendering to the wrong buffer
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            Log.e(FRAME_BUFFERS_TAG, "FRAMEBUFFER: Framebuffer is not complete!")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    override fun onRender(models: List<Model>, uniforms: Uniforms) {

        // Bind so the next gl calls write into this buffer
        glViewport(0, 0, fbWidth, fbHeight)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)

        // Render all models
        models.forEach { it.onRender(uniforms) }

        // Now that the rendering has been completed, we bind the texture to the quad. The model
        // has only 1 mesh and 1 texture, so we bind the computed texture to texture idx 0
        ndcQuad.changeTextureAtIdx(0, texture[0])

        // Reset state for the final post-processing rendering
        unbindFrameBuffer()

    }

    fun quadAsList() = listOf(ndcQuad)

    fun renderQuad() {
        ndcQuad.onRenderWithProgram(shaderProgram, ndcUniforms)
    }

}