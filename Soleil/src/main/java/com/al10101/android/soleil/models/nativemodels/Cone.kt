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

        val coneMeshes = computeDefaultMeshes(height, slices, radius, cap, rgb, alpha)
        coneMeshes.forEach {
            meshes.add(it)
            meshIdxWithProgram.add(0) // index (mesh) 0 with program 0
        }

        // All meshes linked to the only program
        programs.add(program)

        // Add the same texture to the 3 meshes
        textureId?.let {
            for (i in coneMeshes.indices) {
                textureIds.add(it)
                textureIdIdxWithMeshIdx.add(i) // texture idx i is linked to mesh idx i
            }
        }

        // Link the only child to the mesh
        super.add(
            ChildNode(position, rotation, scale).apply {
                for (i in coneMeshes.indices) {
                    meshesIndices.add(i) // <- This child is linked to the mesh nr. 0
                }
            }
        )

    }

    companion object {

        fun computeDefaultMeshes(
            height: Float, slices: Int, radius: Float,
            cap: Boolean, rgb: RGB, alpha: Float
        ): List<Mesh> {

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
            val coneFaces = mutableListOf<Face>()
            val capFaces = mutableListOf<Face>()
            var coneFaceOffset = 0
            var capFaceOffset = 0

            // This model will contain 2 meshes, so we initialize 2 vertex arrays
            val coneVertices = FloatArray(TOTAL_COMPONENTS_COUNT * coneStride)
            var capVertices: FloatArray? = null

            if (cap) {
                capVertices = FloatArray(TOTAL_COMPONENTS_COUNT * capStride)
            }

            var coneOffset = 0
            var capOffset = 0

            // Add the center to the cap
            capVertices?.let {
                // Position
                it[capOffset++] = 0f
                it[capOffset++] = 0f
                it[capOffset++] = 0f
                // Color
                it[capOffset++] = rgb.r
                it[capOffset++] = rgb.g
                it[capOffset++] = rgb.b
                it[capOffset++] = alpha
                // Normal
                it[capOffset++] = 0f
                it[capOffset++] = -1f
                it[capOffset++] = 0f
                // Texture
                it[capOffset++] = 0.5f
                it[capOffset++] = 0.5f
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
                coneVertices[coneOffset++] = x
                coneVertices[coneOffset++] = 0f
                coneVertices[coneOffset++] = z
                // Color
                coneVertices[coneOffset++] = rgb.r
                coneVertices[coneOffset++] = rgb.g
                coneVertices[coneOffset++] = rgb.b
                coneVertices[coneOffset++] = alpha
                // Normal
                coneVertices[coneOffset++] = -coneY * cosTheta
                coneVertices[coneOffset++] = coneX
                coneVertices[coneOffset++] = -coneY * sinTheta
                // Texture
                coneVertices[coneOffset++] = texX
                coneVertices[coneOffset++] = 0f

                // Top coordinates of the cone
                // Position
                coneVertices[coneOffset++] = 0f
                coneVertices[coneOffset++] = height
                coneVertices[coneOffset++] = 0f
                // Color
                coneVertices[coneOffset++] = rgb.r
                coneVertices[coneOffset++] = rgb.g
                coneVertices[coneOffset++] = rgb.b
                coneVertices[coneOffset++] = alpha
                // Normal
                coneVertices[coneOffset++] = -coneY * cosTheta
                coneVertices[coneOffset++] = coneX
                coneVertices[coneOffset++] = -coneY * sinTheta
                // Texture
                coneVertices[coneOffset++] = 0.5f
                coneVertices[coneOffset++] = 1f

                // Add the faces. If it is the last slice, close the triangle with the first index
                val nextFaceIdx = if (thetaIdx == slices-1) { 0 } else { coneFaceOffset + 2 }
                val coneFace = Face(coneFaceOffset, coneFaceOffset+1, nextFaceIdx)
                coneFaces.add(coneFace)
                coneFaceOffset += 2

                // Cap
                capVertices?.let {
                    // Face maintaining counter clockwise order pointing downwards
                    val nextFaceIdx1 = if (thetaIdx == slices-1) { 1 } else { capFaceOffset + 1 }
                    val capFace = Face(nextFaceIdx1, 0, capFaceOffset)
                    capFaces.add(capFace)
                    capFaceOffset ++
                    // Position
                    it[capOffset++] = x
                    it[capOffset++] = 0f
                    it[capOffset++] = z
                    // Color
                    it[capOffset++] = rgb.r
                    it[capOffset++] = rgb.g
                    it[capOffset++] = rgb.b
                    it[capOffset++] = alpha
                    // Normal
                    it[capOffset++] = 0f
                    it[capOffset++] = -1f
                    it[capOffset++] = 0f
                    // Texture
                    it[capOffset++] = 0.5f + cosTheta * 0.5f
                    it[capOffset++] = 0.5f + sinTheta * 0.5f
                }

            }

            val coneMesh = Mesh(coneVertices, coneFaces)
            val capMesh = capVertices?.let { Mesh(it, capFaces) }

            val meshes = mutableListOf<Mesh>()
            meshes.add(coneMesh)
            capMesh?.let { meshes.add(it) }

            return meshes

        }

        fun getMeshes(
            height: Float, slices: Int, radius: Float,
            cap: Boolean, rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): List<Mesh> {

            // Default meshes defined at origin
            val coneMeshes = computeDefaultMeshes(height, slices, radius, cap, rgb, alpha)

            // Declare transformation variables
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }

            coneMeshes.forEach {
                it.updatePositionAndNormal(position, modelMatrix, temp)
            }

            return coneMeshes

        }

    }

}