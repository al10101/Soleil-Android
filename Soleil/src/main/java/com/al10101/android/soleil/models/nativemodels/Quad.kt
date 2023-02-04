package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.extensions.updatePositionAndNormal
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram

class Quad @JvmOverloads constructor(
    width: Float, height: Float,
    program: ShaderProgram,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    clipS: Float = 0f,
    clipT: Float = 0f,
    textureId: Int? = null,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Quad"
): Model(name) {

    init {

        // Add only 1 mesh to the RootNode
        val quadMesh = getMesh(width, height, rgb, alpha, clipS, clipT, Vector.zero, Quaternion.upY, Vector.one)
        meshes.add(quadMesh)

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

        fun getMesh(
            width: Float, height: Float,
            rgb: RGB, alpha: Float,
            clipS: Float, clipT: Float,
            position: Vector, rotation: Quaternion, scale: Vector
        ): Mesh {

            val wHalf = width / 2f
            val hHalf = height / 2f

            // Since it will be drawn in the fan mode, we add positions and fan. The figure is centered
            // at the origin and pointing towards the viewer. Order of coordinates: XYZ RGBA XYZ ST
            val vertexData = floatArrayOf(
                -wHalf, -hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 0f+clipS, 0f+clipT,
                -wHalf,  hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 0f+clipS, 1f-clipT,
                 wHalf,  hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 1f-clipS, 1f-clipT,
                 wHalf, -hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 1f-clipS, 0f+clipT
            )

            // Modify the array before passing it to the mesh
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }
            vertexData.updatePositionAndNormal(position, modelMatrix, temp)

            val faces = listOf(
                Face(0, 1, 2),
                Face(0, 2, 3)
            )

            return Mesh(vertexData, faces)

        }

    }

}