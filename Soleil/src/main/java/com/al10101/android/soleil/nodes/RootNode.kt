package com.al10101.android.soleil.nodes

import com.al10101.android.soleil.models.Mesh

open class RootNode: Node() {

    var meshes: MutableList<Mesh> = mutableListOf()

    var textureIds: MutableList<Int> = mutableListOf()
    var textureIdIdxWithMeshIdx: MutableList<Int> = mutableListOf()

}