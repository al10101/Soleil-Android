package com.al10101.android.soleil.nodes

import android.opengl.Matrix.multiplyMM
import android.util.Log
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.utils.MODELS_TAG

open class Node {

    var parent: Node? = null
    var children: MutableList<ChildNode> = mutableListOf()

    fun add(childNode: ChildNode) {
        children.add(childNode)
        childNode.parent = this
    }

    fun remove(childNode: ChildNode) {
        childNode.children.forEach {
            it.parent = this
            children.add(it)
        }
        val index = children.indexOf(
            children.find { it == childNode } ?: return
        )
        children.removeAt(index)
        childNode.parent = null
        childNode.children = mutableListOf()
    }

    fun renderEveryChild(temp: FloatArray, uniforms: Uniforms, model: Model) {

        // Store the original modelMatrix from the uniforms
        val globalModelMatrix = uniforms.modelMatrix.copyOf()

        children.forEach { childNode ->

            // Pass total movement to the modelMatrix from the node
            multiplyMM(temp, 0, globalModelMatrix, 0, childNode.modelMatrix, 0)
            uniforms.modelMatrix = temp

            // The number of meshes is equal or greater than the number of programs, since
            // there shouldn't be 2 programs linked to the same mesh
            childNode.meshesIndices.forEach { meshIdx ->

                // The meshIdx is the index to identify the program inside the mesIdxWithProgram variable
                val programIdx = model.meshIdxWithProgram[meshIdx]

                val program = model.programs[programIdx]
                val mesh = model.meshes[meshIdx]

                // The number of textures is less, equal or greater than the number of meshes, since
                // there can be 2 textures linked to the same mesh
                val mutableTextures = mutableListOf<Int>()
                model.textureIdIdxWithMeshIdx.forEachIndexed { i, it ->
                    if (it == meshIdx) {
                        mutableTextures.add(model.textureIds[i])
                    }
                }

                program.useProgram()
                program.setUniforms(uniforms, mutableTextures)
                mesh.bindData(program)
                mesh.draw()

            }

            // Reset to original value for next children recursively
            uniforms.modelMatrix = globalModelMatrix
            childNode.renderEveryChild(temp, uniforms, model)

        }

        // Reset to original value for next operations
        uniforms.modelMatrix = globalModelMatrix

    }

    fun renderEveryChildWithProgram(program: ShaderProgram, temp: FloatArray, uniforms: Uniforms, model: Model) {

        // Store the original modelMatrix from the uniforms
        val globalModelMatrix = uniforms.modelMatrix.copyOf()

        children.forEach { childNode ->

            // Pass total movement to the modelMatrix from the node
            multiplyMM(temp, 0, globalModelMatrix, 0, childNode.modelMatrix, 0)
            uniforms.modelMatrix = temp

            // The number of meshes is equal or greater than the number of programs, since
            // there shouldn't be 2 programs linked to the same mesh
            childNode.meshesIndices.forEach { meshIdx ->

                val mesh = model.meshes[meshIdx]

                // The number of textures is less, equal or greater than the number of meshes, since
                // there can be 2 textures linked to the same mesh
                val mutableTextures = mutableListOf<Int>()
                model.textureIdIdxWithMeshIdx.forEachIndexed { i, it ->
                    if (it == meshIdx) {
                        mutableTextures.add(model.textureIds[i])
                    }
                }

                program.useProgram()
                program.setUniforms(uniforms, mutableTextures)
                mesh.bindData(program)
                mesh.draw()

            }

            // Reset to original value for next children recursively
            uniforms.modelMatrix = globalModelMatrix
            childNode.renderEveryChildWithProgram(program, temp, uniforms, model)

        }

        // Reset to original value for next operations
        uniforms.modelMatrix = globalModelMatrix

    }

    fun setNewModelMatrixForEveryChild(temp: FloatArray, modelMatrix: FloatArray) {
        children.forEach { childNode ->
            multiplyMM(temp, 0, modelMatrix, 0, childNode.modelMatrix, 0)
            for (i in 0 until 16) {
                childNode.modelMatrix[i] = temp[i]
            }
            childNode.setNewModelMatrixForEveryChild(temp, modelMatrix)
        }
    }

    fun updateMeshesIndicesForEveryChild(new0: Int) {
        children.forEach { childNode ->
            for (i in childNode.meshesIndices.indices) {
                childNode.meshesIndices[i] += new0
            }
            childNode.updateMeshesIndicesForEveryChild(new0)
        }
    }

    fun logEveryChild(TAG: String, modelName: String, marker: String) {
        Log.i(TAG, "$modelName:${marker}child has ${children.size} children")
        children.forEachIndexed { i, childNode ->
            Log.i(TAG, "$modelName:${marker}child $i linked to meshes ${childNode.meshesIndices}")
            childNode.logEveryChild(TAG, modelName, "$marker-")
        }
    }

}