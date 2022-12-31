package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class Sphere @JvmOverloads constructor(
    program: ShaderProgram,
    stacks: Int, slices: Int, radius: Float,
    rgb: RGB = RGB.white,
    alpha: Float = 1f,
    position: Vector = Vector.zero,
    rotation: Quaternion = Quaternion.upY,
    scale: Vector = Vector.one,
    name: String = "Sphere"
): Model(name) {

    init {

        val pi = PI.toFloat()

        // Initialize counter. The order of the coordinates is as follows:
        // XYZ RGBA XYZ ST
        // This means that there are 12 components for each vertex
        val totalComponentCount = 12
        val vertices = FloatArray(totalComponentCount * (slices * 2 + 2) * stacks)
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
        meshes.add(
            Mesh(vertices, totalFanElements)
        )

        // Also add the program to the model
        programs.add(program)
        meshIdxWithProgram.add(0) // <- The only program is linked to the mesh nr. 0

        // Link the only child to the mesh
        children.add(
            ChildNode(position, rotation, scale).apply {
                meshesIndices.add(0) // <- This child is linked to the mesh nr. 0
            }
        )

    }

}