package com.al10101.android.soleil.models

import com.al10101.android.soleil.utils.MAX_SHORT_VALUE

data class MeshContainer(
    val vertexData: FloatArray,
    val faces: List<Face>
) {

    fun mergeDataTo(verticesList: MutableList<Float>, faces: MutableList<Face>): Boolean {
        // The maxIdx at the moment is the number of vertices before the addition
        val maxIdx = verticesList.size / TOTAL_COMPONENT_COUNT
        // When adding lists this way, the max number for an integer can be passed. So, before adding
        // more triangles to the same mesh, check if that would be possible. If, after the addition
        // of vertices, the maxIdx would be greater than the shot limit, do not add anything
        val thisVertices = this.vertexData.size / TOTAL_COMPONENT_COUNT
        // The maximum number that a short data type can store is 32767. That is equal to the number of
        // vertices that a single mesh can have to be processed with the Mesh.draw() method
        if (maxIdx + thisVertices > MAX_SHORT_VALUE) {
            return false
        }
        // If the code reaches this part, it means that it is safe to merge data
        // First the vertices
        this.vertexData.forEach { verticesList.add(it) }
        // Now faces. Consider the addition of each new added index
        this.faces.forEach {
            val newFace = Face(it.a + maxIdx, it.b + maxIdx, it.c + maxIdx)
            faces.add(newFace)
        }
        return true
    }

    fun toMesh() = Mesh(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MeshContainer
        if (!vertexData.contentEquals(other.vertexData)) return false
        if (faces != other.faces) return false
        return true
    }
    override fun hashCode(): Int {
        var result = vertexData.contentHashCode()
        result = 31 * result + faces.hashCode()
        return result
    }
}