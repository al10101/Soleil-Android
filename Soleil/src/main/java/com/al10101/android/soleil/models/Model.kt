package com.al10101.android.soleil.models

import android.opengl.Matrix.multiplyMM
import com.al10101.android.soleil.data.Uniforms
import com.al10101.android.soleil.programs.ShaderProgram
import java.lang.IllegalStateException

open class Model(
    override var name: String
): RootNode(), Renderable {

    var programs: MutableList<ShaderProgram> = mutableListOf()
    var programIdxWithMesh: MutableList<Int> = mutableListOf()
    // programIdxWithMesh -> {
    // index0<Program> works with value0<Mesh>
    // index1<Program> works with value1<Mesh>
    // ...
    // }
    // That is, programs.size and programIdxWithMesh.size must be the same value.
    // The max number in programIdxWithMesh corresponds to meshes.size-1

    override fun onRender(uniforms: Uniforms) {

        val temp = FloatArray(16)
        // Store the original modelMatrix from the uniforms
        val globalModelMatrix = uniforms.modelMatrix.copyOf()

        children.forEach { childNode ->

            // Pass total movement to the modelMatrix from the node
            multiplyMM(temp, 0, globalModelMatrix, 0, childNode.modelMatrix, 0)
            uniforms.modelMatrix = temp

            // The number of meshes is equal or greater than the number of programs, since
            // there cannot be 2 programs linked to the same mesh. So we start by counting the meshes
            // inside the childNode
            childNode.meshesIndices.forEach { meshIdx ->
                
                // We find the meshIdx as a (unique) value inside the list of indexes
                val programIdx = programIdxWithMesh.indexOf(meshIdx)

                val program = programs[programIdx]
                val mesh = meshes[meshIdx]

                program.useProgram()
                program.setUniforms(uniforms)
                mesh.bindData(program)
                mesh.drawIndexedTriangles()

            }

        }

    }

}