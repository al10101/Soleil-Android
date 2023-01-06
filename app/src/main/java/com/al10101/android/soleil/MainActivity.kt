package com.al10101.android.soleil

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.al10101.android.soleil.custom.TouchableGLView

class MainActivity: AppCompatActivity() {

    private lateinit var glView: GLSurfaceView
    private var rendererSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = TouchableGLView(this)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(SnowmanRenderer(this))
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