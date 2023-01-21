package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram

open class Box @JvmOverloads constructor(
    width: Float, height: Float, depth: Float,
    program: ShaderProgram,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    textureId: Int? = null,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Box"
): Model(name) {

    init {

        val boxMeshes = computeDefaultMeshes(width, height, depth, rgb, alpha)
        boxMeshes.forEach {
            meshes.add(it)
            meshIdxWithProgram.add(0) // index (mesh) 0 with program 0
        }

        // All meshes linked to the only program
        programs.add(program)

        // Add the same texture to the 3 meshes
        textureId?.let {
            for (i in boxMeshes.indices) {
                textureIds.add(it)
                textureIdIdxWithMeshIdx.add(i) // texture idx i is linked to mesh idx i
            }
        }

        // Link the only child to the mesh
        super.add(
            ChildNode(position, rotation, scale).apply {
                for (i in boxMeshes.indices) {
                    meshesIndices.add(i) // <- This child is linked to the mesh nr. 0
                }
            }
        )

    }

    companion object {

        fun computeDefaultMeshes(
            width: Float, height: Float, depth: Float,
            rgb: RGB, alpha: Float
        ): List<Mesh> {

            val x = width / 2f
            val y = height / 2f
            val z = depth / 2f

            // The model consists of 6 meshes. The order is:
            // 1 - - - 2
            // |       |
            // |       |
            // 0 - - - 3
            // Order of the coordinates: XYZ RGBA XYZ ST
            // The coordinates are made so that the whole box shares the same texture

            // Left -> always -x, the fan is in the YZ plane
            val leftFan = floatArrayOf(
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.00f, 0.33f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.00f, 0.66f,
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.25f, 0.66f,
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.25f, 0.33f
            )
            // Right -> always +x, the fan is in the inverted YZ plane
            val rightFan = floatArrayOf(
                x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.50f, 0.33f,
                x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.50f, 0.66f,
                x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.75f, 0.66f,
                x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.75f, 0.33f
            )
            // Front -> always +z, the fan is in the XY plane
            val frontFan = floatArrayOf(
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.25f, 0.33f,
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.25f, 0.66f,
                 x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.50f, 0.66f,
                 x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.50f, 0.33f
            )
            // Back -> always -z, the fan in in the inverted XY plane
            val backFan = floatArrayOf(
                 x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 0.75f, 0.33f,
                 x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 0.75f, 0.66f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 1.00f, 0.66f,
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 1.00f, 0.33f
            )
            // Top -> always +y, the fan is in XZ plane
            val topFan = floatArrayOf(
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.25f, 0.66f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.25f, 1.00f,
                 x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.50f, 1.00f,
                 x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.50f, 0.66f
            )
            // Bottom -> always -y, the fan is in inverted XZ plane
            val bottomFan = floatArrayOf(
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.25f, 0.00f,
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.25f, 0.33f,
                 x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.50f, 0.33f,
                 x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.50f, 0.00f
            )

            // Since all coordinates are designed the same, all faces share the same fan order
            val faces = listOf(
                Face(0, 1, 2),
                Face(0, 2, 3)
            )

            val meshes: MutableList<Mesh> = mutableListOf()
            meshes.apply {
                add( Mesh(leftFan, faces) )
                add( Mesh(rightFan, faces) )
                add( Mesh(frontFan, faces) )
                add( Mesh(backFan, faces) )
                add( Mesh(topFan, faces) )
                add( Mesh(bottomFan, faces) )
            }

            return meshes

        }

        fun getMeshes(
            width: Float, height: Float, depth: Float,
            rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): List<Mesh> {

            // Default meshes defined at origin
            val boxMeshes = computeDefaultMeshes(width, height, depth, rgb, alpha)

            // Declare transformation variables
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }

            boxMeshes.forEach {
                it.updatePositionAndNormal(position, modelMatrix, temp)
            }

            return boxMeshes

        }

    }

}