package com.al10101.android.soleil

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.android.soleil.custom.*
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.loadTexture
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Quad
import com.al10101.android.soleil.models.nativemodels.Sphere
import com.al10101.android.soleil.nativemodels.Snowman
import com.al10101.android.soleil.programs.LambertianShaderProgram
import com.al10101.android.soleil.programs.SimpleTextureShaderProgram
import com.al10101.android.soleil.uniforms.Camera
import com.al10101.android.soleil.uniforms.Light
import com.al10101.android.soleil.uniforms.LightArray
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FrameRate.limitFrameRate
import com.al10101.android.soleil.utils.FrameRate.logFrameRate
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "SnowmanRenderer"

class SnowmanRenderer(private val context: Context): TouchableGLRenderer {

    override lateinit var camera: Camera
    override lateinit var controls: Controls
    override lateinit var models: MutableList<Model>
    override lateinit var uniforms: Uniforms
    override var zoomMode: ZoomMode = ZoomMode.POSITION
    override var dragMode: DragMode = DragMode.TRANSLATION
    override var maxNorm: Float = 0f

    private var globalStartTime: Long = 0

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Turning off any dithering
        glDisable(GL_DITHER)

        // "De-select" faces that are aimed away from us
        glEnable(GL_CULL_FACE)
        glCullFace(GL_FRONT)

        // z-buffering
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val bgColor = RGB.white
        glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)

        // Set the models
        val program = LambertianShaderProgram(context)
        val snowman = Snowman(program)
        val ground = Quad(20f, 20f, program,
            rotation = Quaternion(Vector.unitY, Vector.unitZ.negative()),
        )

        models = mutableListOf(
            snowman, //ground
        )

        models.add(
            Quad(7f, 7f, program, rgb=RGB.red)
        )

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        maxNorm = 3.5f
        controls = Controls()
        camera = Camera(
            position = Vector(0f, 3.5f, 12f),
            center = Vector(0f, 3.5f, 0f),
            aspect = ratio
        ).apply {
            setViewMatrix()
            setPerspectiveProjectionMatrix()
            setViewProjectionMatrix()
            invertViewProjectionMatrix()
        }

        val sunlight = Light(position=Vector(0.5f, 1f, 5f))
        uniforms = Uniforms(
            FloatArray(16), // Empty model matrix
            camera.viewMatrix,
            camera.projectionMatrix,
            camera.position,
            LightArray(sunlight)
        )

        globalStartTime = System.nanoTime()

    }

    override fun onDrawFrame(p0: GL10?) {
        limitFrameRate(15)
        logFrameRate(TAG)

        // Move the whole scene
        uniforms.modelMatrix = controls.dragMatrix.copyOf()

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        models.forEach { it.onRender(uniforms) }

    }

}