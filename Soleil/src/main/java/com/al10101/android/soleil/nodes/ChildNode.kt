package com.al10101.android.soleil.nodes

import android.opengl.Matrix
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.rotation
import com.al10101.android.soleil.extensions.scaling
import com.al10101.android.soleil.extensions.translation

open class ChildNode(
    var position: Vector,
    var rotation: Quaternion,
    var scale: Vector
): Node() {

    var meshesIndices: MutableList<Int> = mutableListOf()

    val modelMatrix by lazy {

        val translateMatrix = FloatArray(16).apply { translation(position) }
        val rotateMatrix = FloatArray(16).apply { rotation(rotation) }
        val scaleMatrix = FloatArray(16).apply { scaling(scale) }

        val resultMatrix = FloatArray(16)
        val scalingRotation = FloatArray(16)
        Matrix.multiplyMM(scalingRotation, 0, rotateMatrix, 0, scaleMatrix, 0)
        Matrix.multiplyMM(resultMatrix, 0, translateMatrix, 0, scalingRotation, 0)

        resultMatrix
    }

}
