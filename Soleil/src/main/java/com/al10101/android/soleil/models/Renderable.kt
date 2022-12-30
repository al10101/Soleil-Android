package com.al10101.android.soleil.models

import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms

interface Renderable {
    val name: String
    fun onRender(uniforms: Uniforms)
    fun onRenderWithProgram(program: ShaderProgram, uniforms: Uniforms)
}