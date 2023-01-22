package com.al10101.android.soleil

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.multiplyMV
import android.util.Log
import com.al10101.android.soleil.custom.Controls
import com.al10101.android.soleil.custom.DragMode
import com.al10101.android.soleil.custom.TouchableGLRenderer
import com.al10101.android.soleil.custom.ZoomMode
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Rectangle
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.loadTexture
import com.al10101.android.soleil.extensions.toVector
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
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

private const val TAG = "CursedRoomRenderer"

private const val CURSED_WALL_NAME = "CursedWall"

class CursedRoomRenderer(private val context: Context): TouchableGLRenderer {

    override lateinit var models: MutableList<Model>
    override lateinit var controls: Controls
    override lateinit var camera: Camera
    override lateinit var uniforms: Uniforms
    override var zoomMode: ZoomMode = ZoomMode.PROJECTION
    override var dragMode: DragMode = DragMode.TRANSLATION
    override var maxNorm: Float = 0f

    private lateinit var shadowMapFB: ShadowMapFB

    private lateinit var psychoKernel: KernelShaderProgram
    private lateinit var psychoFB: PostProcessingFB

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

        val sunlightProgram = SunlightShaderProgram(context, 24f)

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
            rotation = Quaternion(Vector.unitX, Vector(1f, 0f, 1f))
        )
        val yellowCone = Cone(4.8f, 40, 1.8f, sunlightProgram,
            rgb = RGB.yellow,
            position = Vector(2.8f, 0f, 3f)
        )

        val sides = 10.5f
        val sidesHalf = sides / 2f
        val sidesQuart = sides / 4f
        val backWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(0f, sidesQuart, -sidesHalf)
        )
        val leftWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(-sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitX, Vector.unitZ.negative())
        )
        val rightWall = Quad(sides, sidesHalf, sunlightProgram,
            position = Vector(sidesHalf, sidesQuart, 0f),
            rotation = Quaternion(Vector.unitZ, Vector.unitX.negative())
        )
        val frontWall = Quad(sides, sidesHalf, textureProgram,
            textureId = textureId,
            position = Vector(0f, sidesQuart, sidesHalf),
            rotation = Quaternion(Vector.unitX, Vector.unitX.negative()),
            name = CURSED_WALL_NAME
        )
        val ceiling = Quad(sides, sides, sunlightProgram,
            position = Vector(0f, sidesHalf, 0f),
            rotation = Quaternion(Vector.unitY, Vector.unitZ)
        )
        val ground = Quad(sides, sides, sunlightProgram,
            rotation = Quaternion(Vector.unitY, Vector.unitZ.negative())
        )

        models = mutableListOf(
            redCylinder,
            greenSphere,
            blueBox,
            yellowCone,

            backWall,
            rightWall,
            leftWall,
            frontWall,
            ceiling,
            ground
        )

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        maxNorm = 8f
        controls = Controls()
        camera = Camera(
            position = Vector(0f, 4f, 12f),
            center = Vector(0f, 0f, -maxNorm),
            aspect = ratio,
            fovy = 45f,
            near = 0.1f,
            far = 50f
        ).apply {
            setViewMatrix()
            setPerspectiveProjectionMatrix()
            setViewProjectionMatrix()
            invertViewProjectionMatrix()
        }

        val sunlight = Light(
            position = Vector(20f, 15f, 10f),
            intensity = 0.2f // For the ambient light intensity
        )
        uniforms = Uniforms(
            FloatArray(16), // empty model matrix
            camera.viewMatrix,
            camera.projectionMatrix,
            camera.position,
            LightArray( sunlight )
        )

        val lightCamera = Camera(
            position = sunlight.position,
            center = Vector.zero,
            up = Vector.unitY,
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

        val kernel = FloatArray(9) // Empty kernel
        psychoKernel = KernelShaderProgram(context, kernel)
        psychoFB = PostProcessingFB(psychoKernel, width, height, width, height)

        globalStartTime = System.nanoTime()

    }

    override fun onDrawFrame(p0: GL10?) {
        logFrameRate(TAG)

        // Move the whole scene
        uniforms.modelMatrix = controls.dragMatrix.copyOf()

        // Render the depth of the scene to get the shadow texture
        shadowMapFB.onRender(models, uniforms)

        // We could perform the rendering now, but to spice the rendering a little bit, we will
        // add a post-processing effect when the cursed wall is looking directly at us.
        // If this is the first frame that the wall is looking at us
        val psychoFactor = howMuchIsTheWallLookingAtUs()

        // Calculate the intensity of the post effect as a function of time
        changePsychoLevelTo(psychoFactor)

        // Apply the new blur to the fb and render
        psychoFB.shaderProgram = psychoKernel
        psychoFB.onRender(models, uniforms)
        psychoFB.renderQuad()

    }

    private fun howMuchIsTheWallLookingAtUs(): Float {

        // Find the model and retrieve the model matrix of its only child node
        val cursedWallModel = models.find { it.name == CURSED_WALL_NAME } ?: return 0f
        val cursedWallChild = cursedWallModel.children[0]
        val cursedWallModelMatrix = cursedWallChild.modelMatrix

        // Get current transformation = rotation and model matrices
        val currentModelMatrix = FloatArray(16)
        multiplyMM(currentModelMatrix, 0, uniforms.modelMatrix, 0, cursedWallModelMatrix, 0)

        // Apply the current transformation to the original vector to get the current direction
        // of the cursed wall
        val currentNormal = FloatArray(4)
        val originalDir = Vector(0f, 0f, 1f)
        val originalNormal = floatArrayOf(originalDir.x, originalDir.y, originalDir.z, 1f)
        multiplyMV(currentNormal, 0, currentModelMatrix, 0, originalNormal, 0)
        val currentVector = currentNormal.toVector()

        // To know if the camera and the model are seeing each other, calculate:
        // A.dot(B) / ( |A| |B| )
        // If the operation is negative, the cursed wall is looking to the direction of the
        // camera vector. The max value when looking at us is -1
        val proof = originalDir.dot(currentVector) / (originalDir.length() * currentVector.length())
        return if (proof < 0f) { abs(proof) } else { 0f }

    }

    private fun changePsychoLevelTo(psychoFactor: Float) {
        psychoKernel.kernel.apply {
            this[0] = -psychoFactor
            this[1] = -psychoFactor
            this[2] = -psychoFactor
            this[3] = -psychoFactor
            this[4] = 1f + psychoFactor * 9f
            this[5] = -psychoFactor
            this[6] = -psychoFactor
            this[7] = -psychoFactor
            this[8] = -psychoFactor
        }
    }

}