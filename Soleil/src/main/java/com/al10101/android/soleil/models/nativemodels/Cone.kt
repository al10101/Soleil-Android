package com.al10101.android.soleil.models.nativemodels

import android.opengl.GLES20.GL_TRIANGLE_FAN
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

open class Cone @JvmOverloads constructor(
    program: ShaderProgram,
    height: Float, slices: Int, radius: Float,
    cap: Boolean = true,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    position: Vector = Vector.zero,
    rotation: Vector = Vector.zero,
    scale: Vector = Vector.one,
    name: String = "Cone"
): Model(name) {

    init {

        val pi = PI.toFloat()

        // The model consists of 2 circles: the cone and the cap. Because
        // we want the cone to have different normals at the top, the stride is different
        // from the cap
        val coneStride = slices * 3

        // The cap only needs 1 more value to store the center of the circle
        val capStride = slices + 1

        // The order of the coordinates is as follows
        // XYZ RGBA XYZ ST
        // This means that there are 12 components for each vertex.
        // The coordinates are made so that the whole cone shares the same texture
        val totalComponents = 12

        // Because of z-fighting, the cap will look weird if it's perfectly close. There must
        // be a little bit of extra border in it
        val extraBorder = 1.005f

        // The normal of each vertex of the cone is precomputed with trigonometry
        val flankLength = sqrt(radius*radius + height*height)
        val coneX = radius / flankLength
        val coneY = -height / flankLength

        // Since the cone mesh will be rendered in indexed mode, the order of the faces must be stored:
        //    1
        //   /  \
        //  /    \
        // 0 - - - 2
        // Vertices 0 and 2 are located at the bottom of the cone, the top of the cone is in the vertex
        // 1. Since we are doing this because we want each triangle with its own normal different from
        // the rest, we make sure to compute it separately. The faces will be added in each iteration
        val faces = mutableListOf<Face>()
        var faceOffset = 0

        // This model will contain 2 meshes, so we initialize 2 vertex arrays
        val coneVertices = FloatArray(totalComponents * coneStride)
        val capVertices = FloatArray(totalComponents * capStride)

        var coneOffset = 0
        var capOffset = 0

        // Add the center to the cap
        // Position
        capVertices[capOffset++] = 0f
        capVertices[capOffset++] = 0f
        capVertices[capOffset++] = 0f
        // Color
        capVertices[capOffset++] = rgb.r
        capVertices[capOffset++] = rgb.g
        capVertices[capOffset++] = rgb.b
        capVertices[capOffset++] = alpha
        // Normal
        capVertices[capOffset++] = 0f
        capVertices[capOffset++] = -1f
        capVertices[capOffset++] = 0f
        // Texture
        capVertices[capOffset++] = 0.5f
        capVertices[capOffset++] = 0.5f

        for (thetaIdx in 0 until slices) {

            // Pre-computed values
            val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
            val cosTheta = cos(theta)
            val sinTheta = sin(theta)
            val x = radius * cosTheta
            val z = radius * sinTheta
            val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())

            // Bottom coordinates of the cone
            // Position
            coneVertices[coneOffset++] = x
            coneVertices[coneOffset++] = 0f
            coneVertices[coneOffset++] = z
            // Color
            coneVertices[coneOffset++] = rgb.r
            coneVertices[coneOffset++] = rgb.g
            coneVertices[coneOffset++] = rgb.b
            coneVertices[coneOffset++] = alpha
            // Normal
            coneVertices[coneOffset++] = -coneY * cosTheta
            coneVertices[coneOffset++] = coneX
            coneVertices[coneOffset++] = -coneY * sinTheta
            // Texture
            coneVertices[coneOffset++] = texX
            coneVertices[coneOffset++] = 0f

            // Top coordinates of the cone
            // Position
            coneVertices[coneOffset++] = 0f
            coneVertices[coneOffset++] = height
            coneVertices[coneOffset++] = 0f
            // Color
            coneVertices[coneOffset++] = rgb.r
            coneVertices[coneOffset++] = rgb.g
            coneVertices[coneOffset++] = rgb.b
            coneVertices[coneOffset++] = alpha
            // Normal
            coneVertices[coneOffset++] = -coneY * cosTheta
            coneVertices[coneOffset++] = coneX
            coneVertices[coneOffset++] = -coneY * sinTheta
            // Texture
            coneVertices[coneOffset++] = 0.5f
            coneVertices[coneOffset++] = 1f

            // Add the faces. If it is the last slice, close the triangle with the first index
            val nextFaceIdx = if (thetaIdx == slices-1) { 0 } else { faceOffset + 2 }
            val currentFace = Face(faceOffset, faceOffset+1, nextFaceIdx)
            faces.add(currentFace)
            faceOffset += 2

            // Cap
            // Position
            capVertices[capOffset++] = extraBorder * x
            capVertices[capOffset++] = 0f
            capVertices[capOffset++] = extraBorder * z
            // Color
            capVertices[capOffset++] = rgb.r
            capVertices[capOffset++] = rgb.g
            capVertices[capOffset++] = rgb.b
            capVertices[capOffset++] = alpha
            // Normal
            capVertices[capOffset++] = 0f
            capVertices[capOffset++] = -1f
            capVertices[capOffset++] = 0f
            // Texture
            capVertices[capOffset++] = 0.5f + cosTheta * 0.5f
            capVertices[capOffset++] = 0.5f + sinTheta * 0.5f

        }

        val coneMesh = Mesh(coneVertices, faces)
        val capMesh = Mesh(capVertices, capStride, GL_TRIANGLE_FAN)

        meshes.add(coneMesh)
        programs.add(program)
        meshIdxWithProgram.add(0) // index (mesh) 0 with program 0

        if (cap) {
            meshes.add(capMesh)
            meshIdxWithProgram.add(0) // index (mesh) 1 with program 0
        }

        // Link the only child to the mesh
        children.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // This child is linked to the mesh nr. 0
                if (cap) { meshesIndices.add(1) } // This child is linked to the mesh nr. 1
            }
        )

    }

}