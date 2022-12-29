package com.al10101.android.soleil

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity: AppCompatActivity() {

    private lateinit var glView: GLSurfaceView
    private var rendererSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = GLSurfaceView(this)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(BoxesRenderer(this))
        rendererSet = true

        setContentView(glView)

    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glView.onPause()
        }
    }

}