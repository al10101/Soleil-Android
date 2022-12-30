package com.al10101.android.soleil.framebuffers

import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.uniforms.Uniforms

interface FrameBuffer {

    fun onGenerate()

    fun onRender(models: List<Model>, uniforms: Uniforms): Uniforms

}