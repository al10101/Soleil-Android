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
import kotlin.math.sqrt

class Cone @JvmOverloads constructor(
    height: Float, slices: Int, radius: Float,
    program: ShaderProgram,
    cap: Boolean = true,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    textureId: Int? = null,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Cone"
): Model(name) {

    init {

        val coneMeshContainer = getMeshContainer(height, slices, radius, cap, rgb, alpha, Vector.zero, Quaternion.upY, Vector.one)
        meshes.add(coneMeshContainer.toMesh())

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
            cap: Boolean, rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): MeshContainer {
            // Compute model matrix and pass everything to the direct computation
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }
            return getMeshContainer(height, slices, radius, cap, rgb, alpha, position, modelMatrix, temp)
        }

        fun getMeshContainer(
            height: Float, slices: Int, radius: Float,
            cap: Boolean, rgb: RGB, alpha: Float,
            position: Vector, modelMatrix: FloatArray, temp: FloatArray
        ): MeshContainer {

            val pi = PI.toFloat()

            // The model consists of 2 circles: the cone and the cap. Because
            // we want the cone to have different normals at the top, the stride is different
            // from the cap
            val coneStride = slices * 3

            // The cap only needs 1 more value to store the center of the circle
            val capStride = slices + 1

            // The normal of each vertex of the cone is precomputed
            val flankLength = sqrt(radius*radius + height*height)
            val coneX = radius / flankLength
            val coneY = -height / flankLength

            // Since the cone mesh will be rendered in indexed mode, the order of the faces must be stored:
            //    1
            //   /  \
            //  /    \
            // 0 - - - 2
            // Vertices 0 and 2 are located at the bottom of the cone, the top of the cone is in the vertex
            // 1. Since we are doing this because we want each triangle with its own normal different from
            // the rest, we make sure to compute it separately. The faces will be added in each iteration
            var nVertices = coneStride
            if (cap) {
                nVertices += capStride
            }
            val vertices = FloatArray(TOTAL_COMPONENT_COUNT * nVertices)
            val faces = mutableListOf<Face>()
            var offset = 0

            if (cap) {
                // Add the center to the cap
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
                // Add the borders of the fan. We repeat this circumference because the
                // normals and texture coordinates are different
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

            for (thetaIdx in 0 until slices) {
                // Pre-computed values
                val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                val cosTheta = cos(theta)
                val sinTheta = sin(theta)
                val x = radius * cosTheta
                val z = radius * sinTheta
                val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())
                // Bottom coordinates of the cone
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
                vertices[offset++] = -coneY * cosTheta
                vertices[offset++] = coneX
                vertices[offset++] = -coneY * sinTheta
                // Texture
                vertices[offset++] = texX
                vertices[offset++] = 0f

                // Top coordinates of the cone
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
                vertices[offset++] = -coneY * cosTheta
                vertices[offset++] = coneX
                vertices[offset++] = -coneY * sinTheta
                // Texture
                vertices[offset++] = 0.5f
                vertices[offset++] = 1f

            }

            // So the order of the vertices is
            // - bottom center
            // - bottom circumference
            // - cone fan
            offset = 0 // Recycle variable to count faces

            // Add bottom fan pointing downwards
            if (cap) {
                for (thetaIdx in 1 until slices) {
                    val nextFaceIdx = if (thetaIdx == slices-1) { 1 } else { thetaIdx + 1 }
                    val bottomFan = Face(nextFaceIdx, 0, thetaIdx)
                    faces.add(bottomFan)
                    offset ++
                }
                offset += 2 // Add 1 to account for the bottom center and 1 because we skipped idx=0
            }

            // Add cone strip
            val initOffset = offset
            for (thetaIdx in 0 until slices - 1) {
                val nextFaceIdx = if (thetaIdx == slices-1) { initOffset } else { offset + 2 }
                val coneFan = Face(offset, offset+1, nextFaceIdx)
                faces.add(coneFan)
                offset += 2
            }

            // Modify the arrays before passing them to the mesh
            vertices.updatePositionAndNormal(position, modelMatrix, temp)

            return MeshContainer(vertices, faces)

        }

    }

}