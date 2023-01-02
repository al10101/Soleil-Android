package com.al10101.android.soleil

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Rectangle
import com.al10101.android.soleil.data.Vector
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

    private lateinit var shadowMapFB: ShadowMapFB

    private lateinit var inversionFB: PostProcessingFB
    private lateinit var blurFB: PostProcessingFB

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

        val bgColor = RGB.grayScale(0.1f)
        glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)

        // Define program
        val sunlightProgram = SunlightShaderProgram(context)
        val textureProgram = SimpleTextureShaderProgram(context)
        val textureId = context.loadTexture(R.drawable.old_obunga)

        // Set the models right away
        val redCylinder = Cylinder(2.4f, 40, 1.3f, sunlightProgram,
            rgb = RGB.red,
            position = Vector(0f, 0f, 1f)
        )
        val greenSphere = Sphere(40, 40, 1.8f, sunlightProgram,
            rgb = RGB.green,
            position = Vector(1.8f, 1.8f, -3f)
        )
        val blueBox = Box(3f, 4f, 1f, sunlightProgram,
            rgb = RGB.blue,
            position = Vector(-3f, 2f, -1.2f),
            rotation = Quaternion(Vector.unitaryX, Vector(1f, 0f, 1f))
        )
        val yellowCone = Cone(4.8f, 40, 1.8f, sunlightProgram,
            rgb = RGB.yellow,
            position = Vector(2.8f, 0f, 3f)
        )

        val sides = 10.5f
        val sidesHalf = sides / 2f
        val sidesQuart = sidesHalf / 2f
        val backWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(0f, sidesQuart, -sidesHalf)
        )
        val leftWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(-sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitaryX, Vector.unitaryZ.negative())
        )
        val rightWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitaryZ, Vector.unitaryX.negative())
        )
        val frontWall = Quad(sides, sidesHalf, textureProgram,
            textureId = textureId,
            position = Vector(0f, sidesQuart, sidesHalf),
            rotation = Quaternion(Vector.unitaryX, Vector.unitaryX.negative())
        )
        val ceiling = Quad(sides, sides, sunlightProgram,
            position = Vector(0f, sidesHalf, 0f),
            rotation = Quaternion(Vector.unitaryY, Vector.unitaryZ)
        )
        val ground = Quad(sides, sides, sunlightProgram,
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
            position = Vector(0f, 4f, 12f),
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
        val lightArray = LightArray( listOf(sunlight) )
        uniforms = Uniforms(
            FloatArray(16), // empty model matrix
            camera.viewMatrix,
            camera.projectionMatrix,
            camera.position,
            lightArray
        )

        val lightCamera = Camera(
            position = sunlight.position,
            center = Vector.zero,
            up = Vector.unitaryY,
            near = 1f,
            far = 50f
        ).apply {
            setViewMatrix()
            setOrthographicProjectionMatrix(
                Rectangle(-10f, 10f, -10f, 10f)
            )
        }
        val lightSpaceUniforms = Uniforms(
            FloatArray(16), // empty model matrix
            lightCamera.viewMatrix,
            lightCamera.projectionMatrix,
            lightCamera.position
        )
        shadowMapFB = ShadowMapFB(context, width*2, height*2, width, height, lightSpaceUniforms)

        inversionFB = PostProcessingFB(
            ShaderProgram(context, R.raw.simple_texture_vs, R.raw.post_inversion),
            width, height, width, height
        )

        blurFB = PostProcessingFB(
            KernelShaderProgram.defaultKernel(context, KernelType.GAUSSIAN_BLUR),
            width, height, width, height
        )

        globalStartTime = System.nanoTime()

    }

    override fun onDrawFrame(p0: GL10?) {
        logFrameRate(TAG)

        // Move the whole scene
        val currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS
        val angle = (currentTime * 15f) % 360f
        uniforms.modelMatrix.rotateY(angle)

        // Render the depth of the scene to get the shadow texture
        shadowMapFB.onRender(models, uniforms)

        // With the shadow texture ready inside the uniforms, render the scene again
        val delta = 25f
        if (angle in (90f-delta)..(270f+delta)) {
            // Render with post-processing effects
            blurFB.onRender(models, uniforms)
            if (angle in (180-delta)..(180+delta)) {
                // Now that the first effect was made, pass it to the second effect and render
                inversionFB.onRender(blurFB.quadAsList(), blurFB.ndcUniforms)
                inversionFB.renderQuad()
            } else {
                // Only render the first effect
                blurFB.renderQuad()
            }
        } else {
            // Render normally
            models.forEach { it.onRender(uniforms) }
        }

    }

}