package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.TOTAL_COMPONENTS_COUNT
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Cylinder @JvmOverloads constructor(
    height: Float, slices: Int, radius: Float,
    program: ShaderProgram,
    bottomCap: Boolean = true,
    topCap: Boolean = true,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    textureId: Int? = null,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Cylinder"
): Model(name) {

    init {

        val cylinderMeshes = computeDefaultMeshes(height, slices, radius, bottomCap, topCap, rgb, alpha)
        cylinderMeshes.forEach {
            meshes.add(it)
            meshIdxWithProgram.add(0) // index (mesh) 0 with program 0
        }

        // All meshes linked to the only program
        programs.add(program)

        // Add the same texture to the 3 meshes
        textureId?.let {
            for (i in cylinderMeshes.indices) {
                textureIds.add(it)
                textureIdIdxWithMeshIdx.add(i) // texture idx i is linked to mesh idx i
            }
        }

        // Link the only child to the mesh
        super.add(
            ChildNode(position, rotation, scale).apply {
                for (i in cylinderMeshes.indices) {
                    meshesIndices.add(i) // <- This child is linked to the mesh nr. 0
                }
            }
        )

    }

    companion object {

        fun computeDefaultMeshes(
            height: Float, slices: Int, radius: Float,
            bottomCap: Boolean, topCap: Boolean,
            rgb: RGB, alpha: Float
        ): List<Mesh> {

            val pi = PI.toFloat()

            // The model consists of 1 hollow tube made up of a "belt" and 2 caps.
            // Since the tube will be rendered with the strip mode, the stride takes into account
            // that 2 triangles are needed for each slice and that the first and last 2 vertices are
            // duplicated
            val tubeStride = slices * 2

            // Both caps will be drawn with the fan mode, so the stride needs only 1 more value to
            // store the center of the circle
            val capStride = slices + 1

            // We need to store the faces of all 3 meshes
            val topFaces = mutableListOf<Face>()
            val bottomFaces = mutableListOf<Face>()
            val tubeFaces = mutableListOf<Face>()

            // The constructor defines the mesh
            val tubeVertices = FloatArray(TOTAL_COMPONENTS_COUNT * tubeStride)
            var bottomVertices: FloatArray? = null
            var topVertices: FloatArray? = null

            if (bottomCap) {
                bottomVertices = FloatArray(TOTAL_COMPONENTS_COUNT * capStride)
            }
            if (topCap) {
                topVertices = FloatArray(TOTAL_COMPONENTS_COUNT * capStride)
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
                tubeVertices[tubeOffset++] = cosTheta
                tubeVertices[tubeOffset++] = 0f
                tubeVertices[tubeOffset++] = sinTheta
                // Texture
                tubeVertices[tubeOffset++] = texX
                tubeVertices[tubeOffset++] = 1f

                // Add the faces. If it is the last slice, close the strip
                val nextCapFaceIdx = if (thetaIdx == slices-1) { 1 } else { thetaIdx + 1 }
                val nextBottomTubeFaceIdx = if (thetaIdx == slices-1) { 0 } else { thetaIdx*2 + 2 }
                val nextTopTubeFaceIdx = if (thetaIdx == slices-1) { 1 } else { thetaIdx*2 + 3 }

                // Add the tube faces in GL_TRIANGLE_STRIP order
                val bottomTriangle = Face(thetaIdx*2, thetaIdx*2 + 1, nextBottomTubeFaceIdx)
                val topTriangle = Face(nextBottomTubeFaceIdx, thetaIdx*2 + 1, nextTopTubeFaceIdx)
                tubeFaces.add(bottomTriangle)
                tubeFaces.add(topTriangle)

                // BottomCap
                bottomVertices?.let {
                    // Face maintaining counter clockwise order pointing downwards
                    val bottomFace = Face(nextCapFaceIdx, 0, thetaIdx)
                    bottomFaces.add(bottomFace)
                    // Position
                    it[bottomOffset++] = x
                    it[bottomOffset++] = 0f
                    it[bottomOffset++] = z
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
                    // Face maintaining counter clockwise order pointing upwards
                    val topFace = Face(thetaIdx, 0, nextCapFaceIdx)
                    topFaces.add(topFace)
                    // Position
                    it[topOffset++] = x
                    it[topOffset++] = height
                    it[topOffset++] = z
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

            }

            // Declare meshes if not null
            val topMesh = topVertices?.let { Mesh(it, topFaces) }
            val tubeMesh = Mesh(tubeVertices, tubeFaces)
            val bottomMesh = bottomVertices?.let { Mesh(it, bottomFaces) }

            val meshes = mutableListOf<Mesh>()
            meshes.add(tubeMesh)
            topMesh?.let { meshes.add(it) }
            bottomMesh?.let { meshes.add(it) }

            return meshes

        }

        fun getMeshes(
            height: Float, slices: Int, radius: Float,
            bottomCap: Boolean, topCap: Boolean,
            rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): List<Mesh> {

            // Default meshes defined at origin
            val cylinderMeshes = computeDefaultMeshes(height, slices, radius, bottomCap, topCap, rgb, alpha)

            // Declare transformation variables
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }

            cylinderMeshes.forEach {
                it.updatePositionAndNormal(position, modelMatrix, temp)
            }

            return cylinderMeshes

        }

    }

}