package com.al10101.android.soleil.models.nativemodels

import android.opengl.GLES20.GL_TRIANGLE_FAN
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Face
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
    bottomCap: Boolean = true,
    topCap: Boolean = true,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
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

        // Because of z-fighting, the caps will look weird if they're perfectly close. There must
        // be a little bit of extra border in them
        val extraBorder = 1.001f

        // The computation of the top cap is not in winding order, so we need to store the faces
        val topFaces = mutableListOf<Face>()
        var faceOffset = 0

        // This model will contain 3 different meshes, so we initialize 3 vertex arrays
        val tubeVertices = FloatArray(totalComponents * tubeStride)
        var bottomVertices: FloatArray? = null
        var topVertices: FloatArray? = null

        if (bottomCap) {
            bottomVertices = FloatArray(totalComponents * capStride)
        }
        if (topCap) {
            topVertices = FloatArray(totalComponents * capStride)
        }

        var tubeOffset = 0
        var bottomOffset = 0
        var topOffset = 0

        // Add the center to the bottom
        bottomVertices?.let {
            // Position
            it[bottomOffset++] = 0f
            it[bottomOffset++] = 0f
            it[bottomOffset++] = 0f
            // Color
            it[bottomOffset++] = rgb.r
            it[bottomOffset++] = rgb.g
            it[bottomOffset++] = rgb.b
            it[bottomOffset++] = alpha
            // Normal
            it[bottomOffset++] = 0f
            it[bottomOffset++] = -1f
            it[bottomOffset++] = 0f
            // Texture
            it[bottomOffset++] = 0.5f
            it[bottomOffset++] = 0.5f
        }

        // Add the center to the top
        topVertices?.let {
            // Position
            it[topOffset++] = 0f
            it[topOffset++] = height
            it[topOffset++] = 0f
            // Color
            it[topOffset++] = rgb.r
            it[topOffset++] = rgb.g
            it[topOffset++] = rgb.b
            it[topOffset++] = alpha
            // Normal
            it[topOffset++] = 0f
            it[topOffset++] = 1f
            it[topOffset++] = 0f
            // Texture
            it[topOffset++] = 0.5f
            it[topOffset++] = 0.5f
        }

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
            tubeVertices[tubeOffset++] = 0f

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
            tubeVertices[tubeOffset++] = 1f

            // BottomCap
            bottomVertices?.let {
                // Position
                it[bottomOffset++] = extraBorder * x
                it[bottomOffset++] = 0f
                it[bottomOffset++] = extraBorder * z
                // Color
                it[bottomOffset++] = rgb.r
                it[bottomOffset++] = rgb.g
                it[bottomOffset++] = rgb.b
                it[bottomOffset++] = alpha
                // Normal
                it[bottomOffset++] = 0f
                it[bottomOffset++] = -1f
                it[bottomOffset++] = 0f
                // Texture
                it[bottomOffset++] = 0.5f + cosTheta * 0.5f
                it[bottomOffset++] = 0.5f + sinTheta * 0.5f
            }

            // TopCap
            topVertices?.let {
                // Position
                it[topOffset++] = extraBorder * x
                it[topOffset++] = height
                it[topOffset++] = extraBorder * z
                // Color
                it[topOffset++] = rgb.r
                it[topOffset++] = rgb.g
                it[topOffset++] = rgb.b
                it[topOffset++] = alpha
                // Normal
                it[topOffset++] = 0f
                it[topOffset++] = 1f
                it[topOffset++] = 0f
                // Texture
                it[topOffset++] = 0.5f + cosTheta * 0.5f
                it[topOffset++] = 0.5f + sinTheta * 0.5f
            }

            // Add the faces. If it is the last slice, close the triangle
            val nextFace = if (thetaIdx == slices-1) { 1 } else { faceOffset + 1 }
            val currentFace = Face(faceOffset, 0, nextFace)
            topFaces.add(currentFace)
            faceOffset ++

        }

        // Create a degenerate triangle to connect stacks and maintain winding order
        for (i in 0 until totalComponents * 2) {
            tubeVertices[tubeOffset++] = tubeVertices[i]
        }

        // Faces only needed for the top mesh
        val topMesh = topVertices?.let { Mesh(it, topFaces) }
        val tubeMesh = Mesh(tubeVertices, tubeStride)
        val bottomMesh = bottomVertices?.let { Mesh(it, capStride, GL_TRIANGLE_FAN) }

        meshes.add(tubeMesh)
        programs.add(program)
        meshIdxWithProgram.add(0) // index (mesh) 0 with program 0

        var capCounter = 0
        bottomMesh?.let {
            meshes.add(it)
            meshIdxWithProgram.add(0) // index (mesh) 1 with program 0
            capCounter ++
        }
        topMesh?.let {
            meshes.add(it)
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