package com.al10101.android.soleil.models

import android.util.Log
import com.al10101.android.soleil.nodes.RootNode
import com.al10101.android.soleil.uniforms.Uniforms
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.utils.MODELS_TAG

open class Model(
    override val name: String
): RootNode(), Renderable {

    val programs: MutableList<ShaderProgram> = mutableListOf()
    val meshIdxWithProgram: MutableList<Int> = mutableListOf()

    // Empty matrix to store all multiplications for every child node
    private val temp = FloatArray(16)

    // meshIdxWithProgram -> {
    // index0<Mesh> works with value0<Program>
    // index1<Mesh> works with value1<Program>
    // ...
    // }
    // That is, meshes.size and meshIdxWithProgram.size must be the same value.
    // The max value inside meshIdxWithProgram corresponds to programs.size-1

    override fun onRender(uniforms: Uniforms) {
        super.renderEveryChild(temp, uniforms, this)
    }

    override fun onRenderWithProgram(program: ShaderProgram, uniforms: Uniforms) {
        super.renderEveryChildWithProgram(program, temp, uniforms, this)
    }

    fun changeTextureAtIdx(idx: Int, newTexture: Int) {
        try {
            textureIds[idx] = newTexture
        } catch (e: IndexOutOfBoundsException) {
            Log.e(MODELS_TAG, "The model $name cannot change texture at index $idx because it has ${textureIds.size} textures")
        }
    }

    fun storeNewPosition(modelMatrix: FloatArray) {
        super.setNewModelMatrixForEveryChild(temp, modelMatrix)
    }

    fun absorbModel(other: Model) {
        //Log.d(MODELS_TAG, "Model $name absorbing model ${other.name}...")
        //Log.d(MODELS_TAG, "$name contains ${meshes.size} meshes, ${textureIds.size} textures and ${programs.size} programs")
        //Log.d(MODELS_TAG, "${other.name} contains ${other.meshes.size} meshes, ${other.textureIds.size} textures and ${other.programs.size} programs")

        // Add every level of abstraction, from general to particular.
        // The inheritance is as follows:
        // MODEL <- ROOT_NODE <- NODE

        // Count the elements inside the lists BEFORE adding more
        val nMeshes = meshes.size
        val nPrograms = programs.size

        // Add at Model leveL:
        other.programs.forEach { programs.add(it) }
        other.meshIdxWithProgram.forEach {
            meshIdxWithProgram.add(nPrograms + it)
        }

        // Now, add at RootNode level:
        other.meshes.forEach {
            meshes.add(it)
        }
        other.textureIds.forEach { textureIds.add(it) }
        other.textureIdIdxWithMeshIdx.forEach {
            textureIdIdxWithMeshIdx.add(nMeshes + it)
        }

        // Finally, add at Node level. This one is tricky because the children
        // are ChildNode objects and each object has meshesIndices and modelMatrix.
        // The modelMatrix doesn't matter and can be added right away, but the meshesIndices
        // must be transformed before adding the child to the current list
        other.updateMeshesIndicesForEveryChild(nMeshes)
        other.children.forEach {
            super.add(it)
        }

        while (other.children.isNotEmpty()) {
            other.remove(other.children[0])
        }

    }

}