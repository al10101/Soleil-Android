package com.al10101.android.soleil.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Cone
import com.al10101.android.soleil.models.nativemodels.Cylinder
import com.al10101.android.soleil.models.nativemodels.Sphere
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.sqrt

class SnowmanHead(
    program: ShaderProgram,
    resolution: Int, radius: Float,
    position: Vector
): Model(name="SnowmanHead") {

    init {

        // The hat has 3 cylinders: 2 for the hat itself and 1 for a little red ribbon
        //    _____
        //   |     |        <- top
        //   |_____|
        // _ i _ _ i _      }- ribbon
        // |_ _ _ _ _|      <- base

        val headColor = RGB.white
        val headSize = radius * 2f
        val headPosition = position.add( Vector.unitY.mul(radius) )

        val hatHeadOffset = radius * 0.6f
        val hatStartsAtY = headSize - hatHeadOffset
        val hatColor = RGB.grayScale(0.2f)

        val baseRadius = radius * 1.4f
        val baseHeight = hatHeadOffset * 0.1f
        val basePosition = position.add( Vector.unitY.mul(hatStartsAtY) )

        val topRadius = radius * 0.92f
        val topHeight = radius * 1.4f
        val topPosition = position.add( Vector.unitY.mul(hatStartsAtY + baseHeight) )

        val ribbonColor = RGB.red
        val ribbonRadius = topRadius * 1.01f
        val ribbonHeight = topHeight * 0.24f
        val ribbonPosition = topPosition.copy()

        // We can also add 2 eyes
        val eyeColor = RGB.black
        val eyeResolution = 5
        val eyeY = radius * 0.05f
        val eyeX = radius * 0.40f
        val eyeZ = sqrt(radius * radius - eyeX * eyeX - eyeY * eyeY )
        val rightEyePosition = position.add(Vector(eyeX, radius + eyeY, eyeZ))
        val leftEyePosition = position.add(Vector(-eyeX, radius + eyeY, eyeZ))
        val eyeRadius = radius * 0.05f

        // Finally, a nose
        val noseColor = RGB(1f, 0.802f, 0.3f)
        val noseLength = radius * 1.1f
        val noseRadius = radius * 0.1f
        val nosePosition = Vector(0f, radius, radius)

        val transparency = 1f

        // We can define all models now
        val head = Sphere(resolution, resolution, radius, program, rgb=headColor, position=headPosition)

        val top = Cylinder(topHeight, resolution, topRadius, program, bottomCap=false, rgb=hatColor, position=topPosition, alpha=transparency)
        val base = Cylinder(baseHeight, resolution, baseRadius, program, rgb=hatColor, position=basePosition, alpha=transparency)
        val ribbon = Cylinder(ribbonHeight, resolution, ribbonRadius, program, rgb=ribbonColor, position=ribbonPosition, alpha=transparency)

        val rightEye = Sphere(eyeResolution, eyeResolution, eyeRadius, program, rgb=eyeColor, position=rightEyePosition)
        val leftEye = Sphere(eyeResolution, eyeResolution, eyeRadius, program, rgb=eyeColor, position=leftEyePosition)

        val nose = Cone(noseLength, resolution, noseRadius, program, rgb=noseColor, position=nosePosition, alpha=transparency,
            rotation= Quaternion(Vector.unitY, Vector.unitZ))

        absorbModel(head)
        absorbModel(top)
        absorbModel(base)
        absorbModel(ribbon)
        absorbModel(rightEye)
        absorbModel(leftEye)
        absorbModel(nose)

    }

}