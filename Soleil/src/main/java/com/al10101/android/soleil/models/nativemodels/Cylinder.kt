package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.extensions.updatePositionAndNormal
import com.al10101.android.soleil.models.*
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

        val cylinderMeshContainer = getMeshContainer(height, slices, radius, bottomCap, topCap, rgb, alpha, Vector.zero, Quaternion.upY, Vector.one)
        meshes.add(cylinderMeshContainer.toMesh())

        // Also add the program to the model
        programs.add(program)
        meshIdxWithProgram.add(0) // <- The program nr. 0 is linked to the mesh with index nr. 0

        // Add the only texture to the only mesh
        textureId?.let {
            textureIds.add(it)
            textureIdIdxWithMeshIdx.add(0)  // texture idx 0 is linked to mesh idx 0
        }

        // Link the only child to the mesh
        super.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // <- This child is linked to the mesh nr. 0
            }
        )

    }

    companion object {

        fun getMeshContainer(
            height: Float, slices: Int, radius: Float,
            bottomCap: Boolean, topCap: Boolean,
            rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): MeshContainer {
            // Compute model matrix and pass everything to the direct computation
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }
            return getMeshContainer(height, slices, radius, bottomCap, topCap, rgb, alpha, position, modelMatrix, temp)
        }

        fun getMeshContainer(
            height: Float, slices: Int, radius: Float,
            bottomCap: Boolean, topCap: Boolean,
            rgb: RGB, alpha: Float,
            position: Vector, modelMatrix: FloatArray, temp: FloatArray
        ): MeshContainer {

            val pi = PI.toFloat()

            // The model consists of 1 hollow tube made up of a "belt" and 2 caps.
            // Since the tube will be rendered with the strip mode, the stride takes into account
            // that 2 triangles are needed for each slice and that the first and last 2 vertices are
            // duplicated
            val tubeStride = slices * 2

            // Both caps will be drawn with the fan mode, so the stride needs only 1 more value to
            // store the center of the circle
            val capStride = slices + 1

            // The constructor defines the number of vertices for the mesh
            var nVertices = tubeStride
            if (bottomCap) {
                nVertices += capStride
            }
            if (topCap) {
                nVertices += capStride
            }

            val vertices = FloatArray(TOTAL_COMPONENT_COUNT * nVertices)
            val faces = mutableListOf<Face>()
            var offset = 0

            if (bottomCap) {
                // Add the center to the bottom
                // Position
                vertices[offset++] = 0f
                vertices[offset++] = 0f
                vertices[offset++] = 0f
                // Color
                vertices[offset++] = rgb.r
                vertices[offset++] = rgb.g
                vertices[offset++] = rgb.b
                vertices[offset++] = alpha
                // Normal
                vertices[offset++] = 0f
                vertices[offset++] = -1f
                vertices[offset++] = 0f
                // Texture
                vertices[offset++] = 0.5f
                vertices[offset++] = 0.5f
                // Add the borders of the fan. We repeat this circumference because
                // the normals and texture coordinates are different
                for (thetaIdx in 0 until slices) {
                    // Pre-computed values
                    val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                    val cosTheta = cos(theta)
                    val sinTheta = sin(theta)
                    val x = radius * cosTheta
                    val z = radius * sinTheta
                    // Position
                    vertices[offset++] = x
                    vertices[offset++] = 0f
                    vertices[offset++] = z
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = 0f
                    vertices[offset++] = -1f
                    vertices[offset++] = 0f
                    // Texture
                    vertices[offset++] = 0.5f + cosTheta * 0.5f
                    vertices[offset++] = 0.5f + sinTheta * 0.5f
                }
            }

            // Add two circumferences, 1 at bottom and 1 at top
            for (i in 0 until 2) {
                val texY = i.toFloat() // T coordinate for texture
                val phi = height * i // either bottom (0f) or top (height)
                for (thetaIdx in 0 until slices) {
                    // Pre-computed values
                    val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                    val cosTheta = cos(theta)
                    val sinTheta = sin(theta)
                    val x = radius * cosTheta
                    val z = radius * sinTheta
                    val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())
                    // Position
                    vertices[offset++] = x
                    vertices[offset++] = phi
                    vertices[offset++] = z
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = cosTheta
                    vertices[offset++] = 0f
                    vertices[offset++] = sinTheta
                    // Texture
                    vertices[offset++] = texX
                    vertices[offset++] = texY
                }
            }

            // Add the center to the top
            if (topCap) {
                // Position
                vertices[offset++] = 0f
                vertices[offset++] = height
                vertices[offset++] = 0f
                // Color
                vertices[offset++] = rgb.r
                vertices[offset++] = rgb.g
                vertices[offset++] = rgb.b
                vertices[offset++] = alpha
                // Normal
                vertices[offset++] = 0f
                vertices[offset++] = 1f
                vertices[offset++] = 0f
                // Texture
                vertices[offset++] = 0.5f
                vertices[offset++] = 0.5f
                // Add the borders of the fan. We repeat this circumference because
                // the normals and texture coordinates are different
                for (thetaIdx in 0 until slices) {
                    // Pre-computed values
                    val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                    val cosTheta = cos(theta)
                    val sinTheta = sin(theta)
                    val x = radius * cosTheta
                    val z = radius * sinTheta
                    // Position
                    vertices[offset++] = x
                    vertices[offset++] = height
                    vertices[offset++] = z
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = 0f
                    vertices[offset++] = 1f
                    vertices[offset++] = 0f
                    // Texture
                    vertices[offset++] = 0.5f + cosTheta * 0.5f
                    vertices[offset++] = 0.5f + sinTheta * 0.5f
                }
            }

            // So the order of the vertices is:
            // - bottom center
            // - bottom circumference
            // - top circumference
            // - top center
            offset = 0 // Recycle variable to count faces

            // Add bottom fan pointing downwards
            if (bottomCap) {
                for (thetaIdx in 1 until slices) {
                    val nextFaceIdx = if (thetaIdx == slices-1) { 1 } else { thetaIdx + 1 }
                    val bottomFan = Face(nextFaceIdx, 0, thetaIdx)
                    faces.add(bottomFan)
                    offset ++
                }
                offset += 2 // Add 1 to account for the bottom center and 1 because we skipped idx=0
            }

            // Add tube strip
            for (thetaIdx in 0 until slices) {
                val nextBottomIdx = if (thetaIdx == slices - 1) { offset - (slices-1) } else { offset + 1 }
                val nextTopIdx = if (thetaIdx == slices - 1) { offset + 1 } else { offset + (slices+1) }
                val bottomTriangle = Face(offset, offset + slices, nextBottomIdx)
                val topTriangle = Face(nextBottomIdx, offset + slices, nextTopIdx)
                faces.add(bottomTriangle)
                faces.add(topTriangle)
                offset ++
            }

            // Add top fan
            if (topCap) {
                val lastIdx = nVertices - 1 // Minus one because we do not count the first index=0
                for (thetaIdx in 0 until slices - 1) {
                    val nextFaceIdx = if (thetaIdx == slices-1) { lastIdx - slices } else { lastIdx - (slices - thetaIdx) + 1 }
                    val topFan = Face(lastIdx - (slices - thetaIdx), lastIdx, nextFaceIdx)
                    faces.add(topFan)
                }
            }

            // Modify the arrays before passing them to the mesh
            vertices.updatePositionAndNormal(position, modelMatrix, temp)

            return MeshContainer(vertices, faces)

        }

    }

}