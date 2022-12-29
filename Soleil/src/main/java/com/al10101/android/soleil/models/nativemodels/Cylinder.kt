package com.al10101.android.soleil.models.nativemodels

import android.opengl.GLES20.GL_TRIANGLE_FAN
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class Cylinder @JvmOverloads constructor(
    program: ShaderProgram,
    height: Float, slices: Int, radius: Float,
    bottomCap: Boolean, topCap: Boolean,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    position: Vector = Vector.zero,
    rotation: Vector = Vector.zero,
    scale: Vector = Vector.one,
    name: String = "Cylinder"
): Model(name) {

    init {

        val pi = PI.toFloat()

        // The model consists of 1 hollow tube made up of a "belt" and 2 caps.
        // Since the tube will be rendered with the strip mode, the stride takes into account
        // that 2 triangles are needed for each slice and that the first and last 2 vertices are
        // duplicated
        val tubeStride = slices * 2 + 2

        // Both caps will be drawn with the fan mode, so the stride needs only 1 more value to
        // store the center of the circle
        val capStride = slices + 1

        // The order of the coordinates is as follows
        // XYZ RGBA XYZ ST
        // This means that there are 12 components for each vertex.
        // The coordinates are made so that the whole cylinder shares the same texture
        val totalComponents = 12

        // This model will contain 3 different meshes, so we initialize 3 vertex arrays
        val tubeVertices = FloatArray(totalComponents * tubeStride)
        val bottomVertices = FloatArray(totalComponents * capStride)
        val topVertices = FloatArray(totalComponents * capStride)

        var tubeOffset = 0
        var bottomOffset = 0
        var topOffset = 0

        // Add the center to the bottom
        // Position
        bottomVertices[bottomOffset++] = 0f
        bottomVertices[bottomOffset++] = 0f
        bottomVertices[bottomOffset++] = 0f
        // Color
        bottomVertices[bottomOffset++] = rgb.r
        bottomVertices[bottomOffset++] = rgb.g
        bottomVertices[bottomOffset++] = rgb.b
        bottomVertices[bottomOffset++] = alpha
        // Normal
        bottomVertices[bottomOffset++] = 0f
        bottomVertices[bottomOffset++] = -1f
        bottomVertices[bottomOffset++] = 0f
        // Texture
        bottomVertices[bottomOffset++] = 0f
        bottomVertices[bottomOffset++] = 0f

        // Add the center to the top
        // Position
        topVertices[topOffset++] = 0f
        topVertices[topOffset++] = height
        topVertices[topOffset++] = 0f
        // Color
        topVertices[topOffset++] = rgb.r
        topVertices[topOffset++] = rgb.g
        topVertices[topOffset++] = rgb.b
        topVertices[topOffset++] = alpha
        // Normal
        topVertices[topOffset++] = 0f
        topVertices[topOffset++] = 1f
        topVertices[topOffset++] = 0f
        // Texture
        topVertices[topOffset++] = 0f
        topVertices[topOffset++] = 0f

        for (thetaIdx in 0 until slices) {

            // Pre-computed values
            val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
            val cosTheta = cos(theta)
            val sinTheta = sin(theta)
            val x = radius * cosTheta
            val z = radius * sinTheta
            val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())

            // First triangle of the tube, initialized at bottom -> origin
            // Position
            tubeVertices[tubeOffset++] = x
            tubeVertices[tubeOffset++] = 0f
            tubeVertices[tubeOffset++] = z
            // Color
            tubeVertices[tubeOffset++] = rgb.r
            tubeVertices[tubeOffset++] = rgb.g
            tubeVertices[tubeOffset++] = rgb.b
            tubeVertices[tubeOffset++] = alpha
            // Normal
            tubeVertices[tubeOffset++] = cosTheta
            tubeVertices[tubeOffset++] = 0f
            tubeVertices[tubeOffset++] = sinTheta
            // Texture
            tubeVertices[tubeOffset++] = texX
            tubeVertices[tubeOffset++] = 1f

            // Second triangle of the tube, initialized at the top
            // Position
            tubeVertices[tubeOffset++] = x
            tubeVertices[tubeOffset++] = height
            tubeVertices[tubeOffset++] = z
            // Color
            tubeVertices[tubeOffset++] = rgb.r
            tubeVertices[tubeOffset++] = rgb.g
            tubeVertices[tubeOffset++] = rgb.b
            tubeVertices[tubeOffset++] = alpha
            // Normal
            tubeVertices[tubeOffset++] = x
            tubeVertices[tubeOffset++] = 0f
            tubeVertices[tubeOffset++] = z
            // Texture
            tubeVertices[tubeOffset++] = texX
            tubeVertices[tubeOffset++] = 0f

            // BottomCap
            // Position
            bottomVertices[bottomOffset++] = x
            bottomVertices[bottomOffset++] = 0f
            bottomVertices[bottomOffset++] = z
            // Color
            bottomVertices[bottomOffset++] = rgb.r
            bottomVertices[bottomOffset++] = rgb.g
            bottomVertices[bottomOffset++] = rgb.b
            bottomVertices[bottomOffset++] = alpha
            // Normal
            bottomVertices[bottomOffset++] = 0f
            bottomVertices[bottomOffset++] = -1f
            bottomVertices[bottomOffset++] = 0f
            // Texture
            bottomVertices[bottomOffset++] = texX
            bottomVertices[bottomOffset++] = 1f

            // TopCap
            // Position
            topVertices[topOffset++] = x
            topVertices[topOffset++] = height
            topVertices[topOffset++] = z
            // Color
            topVertices[topOffset++] = rgb.r
            topVertices[topOffset++] = rgb.g
            topVertices[topOffset++] = rgb.b
            topVertices[topOffset++] = alpha
            // Normal
            topVertices[topOffset++] = 0f
            topVertices[topOffset++] = 1f
            topVertices[topOffset++] = 0f
            // Texture
            topVertices[topOffset++] = texX
            topVertices[topOffset++] = 0f

        }

        // Create a degenerate triangle to connect stacks and maintain winding order
        for (i in 0 until totalComponents * 2) {
            tubeVertices[tubeOffset++] = tubeVertices[i]
        }

        // No faces are needed
        val tubeMesh = Mesh(tubeVertices, tubeStride)
        val topMesh = Mesh(topVertices, capStride, GL_TRIANGLE_FAN)
        val bottomMesh = Mesh(bottomVertices, capStride, GL_TRIANGLE_FAN)

        meshes.add(tubeMesh)
        programs.add(program)
        meshIdxWithProgram.add(0) // index (mesh) 0 with program 0

        var capCounter = 0
        if (bottomCap) {
            meshes.add(bottomMesh)
            meshIdxWithProgram.add(0) // index (mesh) 1 with program 0
            capCounter ++
        }
        if (topCap) {
            meshes.add(topMesh)
            meshIdxWithProgram.add(0) // index (mesh) 2 with program 0
            capCounter ++
        }

        // Link the only child to the mesh
        children.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // <- This child is linked to the mesh nr. 0
                // This cap is always added in first place
                if (bottomCap) { meshesIndices.add(1) } // <- This child is linked to the mesh nr. 1
                // This cap is added in first or second place, we need a counter
                if (topCap) { meshesIndices.add(capCounter) } // <- This child is linked to the mesh nr. capCounter
            }
        )

    }

}