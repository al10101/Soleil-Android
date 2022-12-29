package com.al10101.android.soleil

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.util.Log
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Box
import com.al10101.android.soleil.models.nativemodels.Quad
import com.al10101.android.soleil.programs.SimpleLightShaderProgram
import com.al10101.android.soleil.uniforms.Camera
import com.al10101.android.soleil.uniforms.Light
import com.al10101.android.soleil.uniforms.LightArray
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FrameRate.logFrameRate
import com.al10101.android.soleil.utils.NANOSECONDS
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "BoxesRenderer"

class BoxesRenderer(private val context: Context): GLSurfaceView.Renderer {

    private lateinit var models: List<Model>
    private lateinit var uniforms: Uniforms

    private var globalStartTime: Long = 0

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // "De-select" faces that are aimed away from us
        //glEnable(GL_CULL_FACE)
        //glCullFace(GL_FRONT)

        // z-buffering
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)

        val bgColor = RGB.grayScale(0.1f)
        glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)

        // Define program
        val program = SimpleLightShaderProgram(context)

        // Set the models right away
        val redBox = Box(program, 1f, 1f, 1f, RGB.red)
        val greenBox = Box(program, 1.5f, 1.5f, 1.5f, RGB.green,
            position = Vector(3f, 1f, 1f),
            rotation = Vector(30f, 75f, 84f)
        )
        val blueBox = Box(program, 3f, 3f, 3f, RGB.blue,
            position = Vector(-2f, 3f, -3f),
            rotation = Vector(60f, 32f, 112f)
        )
        val ground = Quad(program, 20f, 20f, RGB.white,
            position = Vector(0f, -0.5f, 0f),
            rotation = Vector(-90f, 0f, 0f)
        )

        models = listOf(
            redBox,
            greenBox,
            blueBox,
            ground,
        )

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()

        val camera = Camera(
            position = Vector(0f, 5f, 12f),
            aspect = ratio,
            fovy = 45f,
            near = 0.1f,
            far = 50f
        ).apply {
            setViewMatrix()
            setPerspectiveProjectionMatrix()
        }

        val sunlight = Light(position=Vector(10f, 20f, 20f))
        val lightArray = LightArray.unrollLights(
            listOf(sunlight)
        )

        uniforms = Uniforms(
            FloatArray(16), // empty model matrix
            camera.viewMatrix,
            camera.projectionMatrix,
            camera.position,
            lightArray,
            null // No textures this time
        )

        globalStartTime = System.nanoTime()

    }

    override fun onDrawFrame(p0: GL10?) {
        logFrameRate(TAG)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Move the whole scene
        val currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS
        val angle = currentTime * 15f

        models.forEach {

            // Since the modelMatrix change inside the onRender() method, we
            // need to reset it before calling it for every model
            setIdentityM(uniforms.modelMatrix, 0)
            rotateM(uniforms.modelMatrix, 0, angle, 0f, 1f, 0f)

            it.onRender(uniforms)

        }

    }

}