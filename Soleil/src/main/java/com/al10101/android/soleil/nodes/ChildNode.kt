package com.al10101.android.soleil.nodes

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix

open class ChildNode(
    var position: Vector,
    var rotation: Quaternion,
    var scale: Vector
): Node() {

    var meshesIndices: MutableList<Int> = mutableListOf()

    val modelMatrix by lazy {
        FloatArray(16).apply {
            toModelMatrix(position, rotation, scale)
        }
    }

}
