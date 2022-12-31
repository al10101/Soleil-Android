package com.al10101.android.soleil

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.identity
import com.al10101.android.soleil.extensions.loadTexture
import com.al10101.android.soleil.extensions.rotateY
import com.al10101.android.soleil.framebuffers.PostProcessingFB
import com.al10101.android.soleil.framebuffers.ShadowMapFB
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.*
import com.al10101.android.soleil.programs.*
import com.al10101.android.soleil.uniforms.Camera
import com.al10101.android.soleil.uniforms.Light
import com.al10101.android.soleil.uniforms.LightArray
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.FrameRate.logFrameRate
import com.al10101.android.soleil.utils.NANOSECONDS
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "BoxesRenderer"

class FiguresRenderer(private val context: Context): GLSurfaceView.Renderer {

    private lateinit var models: List<Model>
    private lateinit var uniforms: Uniforms

    private lateinit var postProcessingFB: PostProcessingFB
    private lateinit var shadowMapFB: ShadowMapFB

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

        // Enable default blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val bgColor = RGB.grayScale(0.1f)
        glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)

        // Define program
        val program = SimpleSunlightShaderProgram(context)
        //val program = SimpleTextureShaderProgram(context)

        // Set the models right away
        val tempV = Vector(3f, -0.222f, 1.001f)
        val redCylinder = Cylinder(program, 2.4f, 40, 1.3f, rgb = RGB.red,
            position = Vector(0f, 0f, 1f)
        )
        val greenSphere = Sphere(program, 40, 40, 1.8f, RGB.green,
            position = Vector(1.8f, 1.8f, -3f)
        )
        val blueBox = Box(program, 3f, 4f, 1f, RGB.blue,
            position = Vector(-3f, 2f, -1.2f),
            rotation = Quaternion(Vector.unitaryX, Vector(1f, 0f, 1f))
        )
        val yellowCone = Cone(program, 4.8f, 40, 1.8f, rgb = RGB.yellow,
            position = Vector(2.8f, 0f, 3f)
        )

        val sides = 10.5f
        val sidesHalf = sides / 2f
        val sidesQuart = sidesHalf / 2f
        val backWall = Quad(program, sides, sidesHalf,
            position = Vector(0f, sidesQuart, -sidesHalf)
        )
        val leftWall = Quad(program, sides, sidesHalf,
            position = Vector(-sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitaryX, Vector.unitaryZ.negative())
        )
        val rightWall = Quad(program, sides, sidesHalf,
            position = Vector(sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitaryZ, Vector.unitaryX.negative())
        )
        val frontWall = Quad(program, sides, sidesHalf,
            position = Vector(0f, sidesQuart, sidesHalf),
            rotation = Quaternion(Vector.unitaryZ, Vector.unitaryZ.negative())
        )
        val ceiling = Quad(program, sides, sides,
            position = Vector(0f, sidesHalf, 0f),
            rotation = Quaternion(Vector.unitaryY, Vector.unitaryZ)
        )
        val ground = Quad(program, sides, sides,
            rotation = Quaternion(Vector.unitaryY, Vector.unitaryZ.negative())
        )

        models = listOf(
            redCylinder,
            greenSphere,
            blueBox,
            yellowCone,

            backWall,
            rightWall,
            leftWall,
            frontWall,
            ceiling,
            ground,
        )

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        val camera = Camera(
            position = Vector(0f, 0.4f, 12f),
            center = Vector(0f, 2f, 0f),
            aspect = ratio,
            fovy = 45f,
            near = 0.1f,
            far = 50f
        ).apply {
            setViewMatrix()
            setPerspectiveProjectionMatrix()
        }

        val sunlight = Light(position=Vector(20f, 15f, 10f))
        val lightArray = LightArray.unrollLights(
            listOf(sunlight)
        )

        uniforms = Uniforms(
            FloatArray(16), // empty model matrix
            camera.viewMatrix,
            camera.projectionMatrix,
            camera.position,
            lightArray,
            intArrayOf(context.loadTexture(R.drawable.old_obunga)) // 1 space for our shadow texture
        )

        shadowMapFB = ShadowMapFB(context, width*2, height*2, width, height, sunlight)
        postProcessingFB = PostProcessingFB(
            //KernelShaderProgram.defaultKernel(context, KernelType.PSYCHEDELIC),
            ShaderProgram(context, R.raw.simple_texture_vs, R.raw.simple_texture_fs),
            width, height, width, height
        )

        globalStartTime = System.nanoTime()

    }

    override fun onDrawFrame(p0: GL10?) {
        logFrameRate(TAG)

        // Move the whole scene
        val currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS
        val angle = currentTime * 15f
        uniforms.modelMatrix.rotateY(angle)

        // Render the depth of the scene to get the shadow texture
        shadowMapFB.onRender(models, uniforms)

        // With the uniforms ready, render the scene again with post-processing effects
        postProcessingFB.onRender(models, uniforms)

    }

}