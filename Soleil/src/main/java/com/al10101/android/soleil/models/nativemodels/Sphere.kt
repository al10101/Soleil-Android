package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.extensions.toVector
import com.al10101.android.soleil.extensions.transform
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.TOTAL_COMPONENTS_COUNT
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class Sphere @JvmOverloads constructor(
    stacks: Int, slices: Int, radius: Float,
    program: ShaderProgram,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    textureId: Int? = null,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Sphere"
): Model(name) {

    init {

        val sphereMesh = computeDefaultMesh(stacks, slices, radius, rgb, alpha)
        meshes.add(sphereMesh)

        // Also add the program to the model
        programs.add(program)
        meshIdxWithProgram.add(0) // <- The only program is linked to the mesh nr. 0

        // Add the only texture to the only mesh
        textureId?.let {
            textureIds.add(it)
            textureIdIdxWithMeshIdx.add(0)  // texture idx 0 is linked to mesh idx 0
        }

        // Link the only child to the mesh
        super.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // <- This child is linked to the mesh nr. 0
            }
        )

    }

    companion object {

        fun computeDefaultMesh(
            stacks: Int, slices: Int, radius: Float,
            rgb: RGB, alpha: Float
        ): Mesh {

            val pi = PI.toFloat()

            // Coordinates:
            // XYZ RGBA XYZ ST
            val vertices = FloatArray(TOTAL_COMPONENTS_COUNT * (slices * 2 + 2) * stacks)
            var offset = 0

            // The outer loop, going from bottom-most stack (or the southern polar regions of our planet
            // or altitude of -90 degrees) and up to the northern pole, at +90 degrees
            for (phiIdx in 0 until stacks) {

                // The first circle
                val phi0 = pi * ( (phiIdx + 0).toFloat() * (1f / stacks.toFloat()) - 0.5f )

                // The next, or second one
                val phi1 = pi * ( (phiIdx + 1).toFloat() * (1f / stacks.toFloat()) - 0.5f )

                // Pre-calculated values
                val cosPhi0 = cos(phi0)
                val sinPhi0 = sin(phi0)
                val cosPhi1 = cos(phi1)
                val sinPhi1 = sin(phi1)

                // The inner loop, going from 0 to 360
                for (thetaIdx in 0 until slices) {

                    // More pre-computed values
                    val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                    val cosTheta = cos(theta)
                    val sinTheta = sin(theta)
                    val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())
                    val stacksM1 = 1f / stacks.toFloat()

                    // First triangle
                    // Position
                    vertices[offset++] = radius * cosPhi0 * cosTheta
                    vertices[offset++] = radius * sinPhi0
                    vertices[offset++] = radius * cosPhi0 * sinTheta
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = cosPhi0 * cosTheta
                    vertices[offset++] = sinPhi0
                    vertices[offset++] = cosPhi0 * sinTheta
                    // Texture
                    vertices[offset++] = texX
                    vertices[offset++] = (phiIdx+0).toFloat() * stacksM1

                    // Second triangle
                    // Position
                    vertices[offset++] = radius * cosPhi1 * cosTheta
                    vertices[offset++] = radius * sinPhi1
                    vertices[offset++] = radius * cosPhi1 * sinTheta
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = cosPhi1 * cosTheta
                    vertices[offset++] = sinPhi1
                    vertices[offset++] = cosPhi1 * sinTheta
                    // Texture
                    vertices[offset++] = texX
                    vertices[offset++] = (phiIdx+1).toFloat() * stacksM1

                }

                // Create a degenerate triangle to connect stacks and maintain winding order
                vertices[offset + 12] = vertices[offset - 12] ; vertices[offset + 0] = vertices[offset + 12]
                vertices[offset + 13] = vertices[offset - 11] ; vertices[offset + 1] = vertices[offset + 13]
                vertices[offset + 14] = vertices[offset - 10] ; vertices[offset + 2] = vertices[offset + 14]

            }

            // The rendering of this object will be made with the strip mode. That is,
            // no faces are required, we only need that the vertices are in the same order of
            // the strip/fan (which they are)
            val totalFanElements = (slices + 1) * 2 * (stacks - 1) + 2

            return Mesh(vertices, totalFanElements)

        }

        fun getMesh(
            stacks: Int, slices: Int, radius: Float,
            rgb: RGB, alpha: Float,
            position: Vector,
            rotation: Quaternion,
            scale: Vector
        ): Mesh {

            // Default mesh defined at origin
            val sphereMesh = computeDefaultMesh(stacks, slices, radius, rgb, alpha)

            // Declare transformation variables
            val temp = FloatArray(4)
            val modelMatrix = FloatArray(16).apply { toModelMatrix(position, rotation, scale) }

            sphereMesh.updatePositionAndNormal(position, modelMatrix, temp)

            return sphereMesh

        }

    }

}