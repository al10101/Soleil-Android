package com.al10101.android.soleil.framebuffers

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.Matrix.multiplyMM
import com.al10101.android.soleil.R
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms

class ShadowMapFB(
    context: Context,
    fbWidth: Int,
    fbHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    val lightSpaceUniforms: Uniforms
): FrameBuffer(
    ShaderProgram(context, R.raw.simple_texture_vs, R.raw.blank_fs),
    fbWidth, fbHeight, screenWidth, screenHeight
) {

    init {
        onGenerate()
    }

    override fun onGenerate() {

        // Configure depth map FBO
        glGenFramebuffers(1, fbo, 0)

        // Create depth texture
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, fbWidth, fbHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null)

        // GL_LINEAR does not make sense for depth texture, using GL_NEAREST
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        // Remove artifact on the edges of the shadow map
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glBindTexture(GL_TEXTURE_2D, 0)

        // Attach depth texture as FBO's depth buffer
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture[0], 0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

    }

    override fun onRender(models: List<Model>, uniforms: Uniforms) {

        // Bind so the next gl calls write into this buffer
        glViewport(0, 0, fbWidth, fbHeight)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo[0])
        glClear(GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)

        // Render all models from the perspective of the sunlight, but with the original
        // global model matrix
        lightSpaceUniforms.modelMatrix = uniforms.modelMatrix.copyOf()
        models.forEach {
            it.onRenderWithProgram(shaderProgram, lightSpaceUniforms)
        }

        // With the rendering now completed, we bind the light space matrix and shadow textures
        uniforms.shadowTextureId = texture[0]
        multiplyMM(uniforms.lightSpaceMatrix, 0, lightSpaceUniforms.projectionMatrix, 0, lightSpaceUniforms.viewMatrix, 0)

        // Reset everything back to normal
        unbindFrameBuffer()

    }


}