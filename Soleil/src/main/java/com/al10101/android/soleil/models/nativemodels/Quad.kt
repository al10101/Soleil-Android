package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram

open class Quad @JvmOverloads constructor(
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

        val wHalf = width / 2f
        val hHalf = height / 2f

        // Since it will be drawn in the fan mode, we add positions and fan. The figure is centered
        // at the origin and pointing towards the viewer. Order of coordinates: XYZ RGBA XYZ ST
        val positions = floatArrayOf(
            -wHalf, -hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 0f+clipS, 0f+clipT,
            -wHalf,  hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 0f+clipS, 1f-clipT,
             wHalf,  hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 1f-clipS, 1f-clipT,
             wHalf, -hHalf, 0f, rgb.r, rgb.g, rgb.b, alpha, 0f, 0f, 1f, 1f-clipS, 0f+clipT
        )

        val faces = listOf(
            Face(0, 1, 2),
            Face(0, 2, 3)
        )

        // Add only 1 mesh to the RootNode
        meshes.add(
            Mesh(positions, faces)
        )

        // Also add the program to the model
        programs.add(program)
        meshIdxWithProgram.add(0) // <- The program nr. 0 is linked to the mesh with index nr. 0

        // Add the only texture to the only mesh
        textureId?.let {
            textureIds.add(it)
            textureIdIdxWithMeshIdx.add(0)  // texture idx 0 is linked to mesh idx 0
        }

        // Link the only child to the mesh
        children.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // <- This child is linked to the mesh nr. 0
            }
        )

    }

}