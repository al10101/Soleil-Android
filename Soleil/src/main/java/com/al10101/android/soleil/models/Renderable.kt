package com.al10101.android.soleil.models

import com.al10101.android.soleil.data.Uniforms

interface Renderable {
    var name: String
    fun onRender(uniforms: Uniforms)
}