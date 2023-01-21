package com.al10101.android.soleil.nativemodels

import android.util.Log
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.programs.ShaderProgram

private const val TAG = "Snowman"

class Snowman(
    program: ShaderProgram,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Snowman"
): Model(name) {

    init {

        val resolution = 40

        val head = SnowmanHead(program, resolution, 1f, Vector.zero)

        // Add each part to the current model
        super.mergeToOneChild(head)

        super.logModel(TAG)

    }

}