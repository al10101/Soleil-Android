package com.al10101.android.soleil.models

import android.opengl.Matrix.multiplyMM
import com.al10101.android.soleil.nodes.RootNode
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.programs.ShaderProgram

open class Model(
    override val name: String
): RootNode(), Renderable {

    var programs: MutableList<ShaderProgram> = mutableListOf()
    var meshIdxWithProgram: MutableList<Int> = mutableListOf()
    // meshIdxWithProgram -> {
    // index0<Mesh> works with value0<Program>
    // index1<Mesh> works with value1<Program>
    // ...
    // }
    // That is, meshes.size and meshIdxWithProgram.size must be the same value.
    // The max value inside meshIdxWithProgram corresponds to programs.size-1

    override fun onRender(uniforms: Uniforms) {

        val temp = FloatArray(16)
        // Store the original modelMatrix from the uniforms
        val globalModelMatrix = uniforms.modelMatrix.copyOf()

        children.forEach { childNode ->

            // Pass total movement to the modelMatrix from the node
            multiplyMM(temp, 0, globalModelMatrix, 0, childNode.modelMatrix, 0)
            uniforms.modelMatrix = temp

            // The number of meshes is equal or greater than the number of programs, since
            // there shouldn't be 2 programs linked to the same mesh
            childNode.meshesIndices.forEach { meshIdx ->

                // The meshIdx is the index to identify the program inside the meshIdxWithProgram variable
                val programIdx = meshIdxWithProgram[meshIdx]

                val program = programs[programIdx]
                val mesh = meshes[meshIdx]

                program.useProgram()
                program.setUniforms(uniforms)
                mesh.bindData(program)
                mesh.draw()

            }

        }

    }

}