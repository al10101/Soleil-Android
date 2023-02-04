package com.al10101.android.soleil.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.nativemodels.Cone
import com.al10101.android.soleil.models.nativemodels.Cylinder
import com.al10101.android.soleil.models.nativemodels.Sphere
import com.al10101.android.soleil.nodes.ChildNode
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

        // We can define all meshes now
        Sphere.getMesh(resolution, resolution, radius, rgb=headColor, alpha=1f,
            position=headPosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { head ->
            super.meshes.add(head)
            super.meshIdxWithProgram.add(0)
        }

        Cylinder.getMeshes(topHeight, resolution, topRadius,
            bottomCap=false, topCap=true, rgb=hatColor, alpha=transparency,
            position=topPosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { topHatMeshes ->
            topHatMeshes.forEach {
                super.meshes.add(it)
                super.meshIdxWithProgram.add(0)
            }
        }

        Cylinder.getMeshes(baseHeight, resolution, baseRadius,
            bottomCap=true, topCap=true, rgb=hatColor, alpha=transparency,
            position=basePosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { bottomHatMeshes ->
            bottomHatMeshes.forEach {
                super.meshes.add(it)
                super.meshIdxWithProgram.add(0)
            }
        }


        Cylinder.getMeshes(ribbonHeight, resolution, ribbonRadius,
            bottomCap=false, topCap=true, rgb=ribbonColor, alpha=transparency,
            position=ribbonPosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { ribbonMeshes ->
            ribbonMeshes.forEach {
                super.meshes.add(it)
                super.meshIdxWithProgram.add(0)
            }
        }

        Sphere.getMesh(eyeResolution, eyeResolution, eyeRadius,
            rgb=eyeColor, alpha=1f,
            position=rightEyePosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { rightEye ->
            super.meshes.add(rightEye)
            super.meshIdxWithProgram.add(0)
        }
        Sphere.getMesh(eyeResolution, eyeResolution, eyeRadius,
            rgb=eyeColor, alpha=1f,
            position=leftEyePosition, rotation= Quaternion.upY, scale= Vector.one
        ).let { leftEye ->
            super.meshes.add(leftEye)
            super.meshIdxWithProgram.add(0)
        }

        Cone.getMeshes(noseLength, resolution, noseRadius,
            cap=false, rgb=noseColor, alpha=transparency,
            position=nosePosition, rotation=Quaternion(Vector.unitY, Vector.unitZ), scale= Vector.one
        ).let { noseMeshes ->
            noseMeshes.forEach {
                super.meshes.add(it)
                super.meshIdxWithProgram.add(0)
            }
        }

        // Add the only program and only child. No textures
        super.programs.add(program)
        super.add(
            ChildNode(Vector.zero, Quaternion.upY, Vector.one).apply {
                super.meshes.indices.forEach { meshesIndices.add(it) }
            }
        )

    }

}