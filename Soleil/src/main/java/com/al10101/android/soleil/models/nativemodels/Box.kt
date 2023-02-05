package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.extensions.updatePositionAndNormal
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.MeshContainer
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram

class Box @JvmOverloads constructor(
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

        val boxMeshContainer = getMeshContainer(width, height, depth, rgb, alpha, Vector.zero, Quaternion.upY, Vector.one)
        meshes.add(boxMeshContainer.toMesh())

        // Also add program to the model
        programs.add(program)
        meshIdxWithProgram.add(0) // index (mesh) 0 with program 0

        // Add the same texture to the mesh
        textureId?.let {
            textureIds.add(it)
            textureIdIdxWithMeshIdx.add(0) // texture idx 0 is linked to mesh idx 0
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
            width: Float, height: Float, depth: Float,
            rgb: RGB, alpha: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): MeshContainer {
            // Compute model matrix and pass everything to the direct computation
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }
            return getMeshContainer(width, height, depth, rgb, alpha, position, modelMatrix, temp)
        }

        fun getMeshContainer(
            width: Float, height: Float, depth: Float,
            rgb: RGB, alpha: Float,
            position: Vector, modelMatrix: FloatArray, temp: FloatArray
        ): MeshContainer {

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

            val vertices = floatArrayOf(
                // Left -> always -x, the fan is in the YZ plane
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.00f, 0.33f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.00f, 0.66f,
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.25f, 0.66f,
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha, -1f,  0f,  0f, 0.25f, 0.33f,
                // Right -> always +x, the fan is in the inverted YZ plane
                x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.50f, 0.33f,
                x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.50f, 0.66f,
                x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.75f, 0.66f,
                x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  1f,  0f,  0f, 0.75f, 0.33f,
                // Front -> always +z, the fan is in the XY plane
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.25f, 0.33f,
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.25f, 0.66f,
                 x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.50f, 0.66f,
                 x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f,  1f, 0.50f, 0.33f,
                // Back -> always -z, the fan in in the inverted XY plane
                 x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 0.75f, 0.33f,
                 x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 0.75f, 0.66f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 1.00f, 0.66f,
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  0f, -1f, 1.00f, 0.33f,
                // Top -> always +y, the fan is in XZ plane
                -x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.25f, 0.66f,
                -x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.25f, 1.00f,
                 x,  y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.50f, 1.00f,
                 x,  y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f,  1f,  0f, 0.50f, 0.66f,
                // Bottom -> always -y, the fan is in inverted XZ plane
                -x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.25f, 0.00f,
                -x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.25f, 0.33f,
                 x, -y,  z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.50f, 0.33f,
                 x, -y, -z, rgb.r, rgb.g, rgb.b, alpha,  0f, -1f,  0f, 0.50f, 0.00f
            )

            // For every side of the box, the order to form it is [0, 1, 2] [0, 2, 3]
            val faces = mutableListOf<Face>()
            for (i in 0 until 6) {
                val offset = i * 4
                faces.add(Face(offset+0, offset+1, offset+2))
                faces.add(Face(offset+0, offset+2, offset+3))
            }


            // Modify the arrays before passing them to the mesh
            vertices.updatePositionAndNormal(position, modelMatrix, temp)

            return MeshContainer(vertices, faces)

        }

    }

}