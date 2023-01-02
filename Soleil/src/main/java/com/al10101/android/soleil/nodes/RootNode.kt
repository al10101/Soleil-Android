package com.al10101.android.soleil.nodes

import com.al10101.android.soleil.models.Mesh

open class RootNode: Node() {

    val meshes: MutableList<Mesh> = mutableListOf()

    val textureIds: MutableList<Int> = mutableListOf()
    val textureIdIdxWithMeshIdx: MutableList<Int> = mutableListOf()

}