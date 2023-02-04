package com.al10101.android.soleil.models.nativemodels

import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.toModelMatrix
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.models.Mesh
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.models.TOTAL_COMPONENTS_COUNT
import com.al10101.android.soleil.nodes.ChildNode
import com.al10101.android.soleil.programs.ShaderProgram
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Sphere @JvmOverloads constructor(
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
            val stacksM1 = 1f / stacks.toFloat()

            // Coordinates:
            // XYZ RGBA XYZ ST
            // We do not consider first and last stack to contribute to the slices, since top
            // and bottom part of the spheres don't have slices, only 1 point each
            if (stacks < 3) {
                throw IllegalArgumentException("The number of stacks in a sphere must be >= 3 ($stacks given)")
            }
            val vertices = FloatArray(TOTAL_COMPONENTS_COUNT * (slices * (stacks-2) + 2))
            var offset = 0

            // The outer loop, going from bottom-most stack (or the southern polar regions of our planet
            // or altitude of -90 degrees) and up to the northern pole, at +90 degrees
            for (phiIdx in 0 until stacks) {

                val phi = pi * ( phiIdx.toFloat() * (1f / (stacks-1).toFloat()) - 0.5f )

                // Pre-calculated values
                val cosPhi0 = cos(phi)
                val sinPhi0 = sin(phi)

                if (phiIdx == 0 || phiIdx == stacks-1) {
                    // Position
                    vertices[offset++] = radius * cosPhi0
                    vertices[offset++] = radius * sinPhi0
                    vertices[offset++] = radius * cosPhi0
                    // Color
                    vertices[offset++] = rgb.r
                    vertices[offset++] = rgb.g
                    vertices[offset++] = rgb.b
                    vertices[offset++] = alpha
                    // Normal
                    vertices[offset++] = cosPhi0
                    vertices[offset++] = sinPhi0
                    vertices[offset++] = cosPhi0
                    // Texture
                    vertices[offset++] = 0.5f
                    vertices[offset++] = phiIdx.toFloat() * stacksM1
                    // Skip the theta loop
                    continue
                }

                // The inner loop, going from 0 to 360
                for (thetaIdx in 0 until slices) {

                    // More pre-computed values
                    val theta = -2f * pi * thetaIdx.toFloat() / (slices - 1).toFloat()
                    val cosTheta = cos(theta)
                    val sinTheta = sin(theta)
                    val texX = thetaIdx.toFloat() * (1f / (slices - 1).toFloat())

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
                    vertices[offset++] = phiIdx.toFloat() * stacksM1

                }

            }

            // We need to store all the faces, depending on the order of the vertices' computation
            val faces = mutableListOf<Face>()
            val lastIdx = slices * (stacks - 2) + 2 - 1 // Minus one because we do not count the first index=0
            offset = 1 // Recycle the variable to count the indexes in the strips. Start at 1 because the 0 is
            // at the bottom fan and is not used to count the strips

            for (phiIdx in 0 until stacks) {

                if (phiIdx == 0) {
                    // If it is the fist phiIdx, compute it considering a fan with the idx=0 as the center
                    for (thetaIdx in 0 until slices) {
                        val nextFaceIdx = if (thetaIdx == slices-1) { 1 } else { thetaIdx + 1 }
                        val bottomFan = Face(nextFaceIdx, 0, thetaIdx)
                        faces.add(bottomFan)
                    }
                }

                else if (phiIdx == stacks-1) {
                    // If it is the last phiIdx, compute it considering a fan with the lastIdx as the center
                    for (thetaIdx in 0 until slices) {
                        val nextFaceIdx = if (thetaIdx == slices-1) { lastIdx - slices } else { lastIdx - (slices - thetaIdx) + 1 }
                        val topFan = Face(lastIdx - (slices - thetaIdx), lastIdx, nextFaceIdx)
                        faces.add(topFan)
                    }
                }

                else if (phiIdx == stacks-2) {
                    // Since all the strips consider the union between the current phiIdx and the
                    // next phiIdx, the penultimate phiIdx shouldn't be considered since the last
                    // phiIdx already considers that row. We skip it
                    continue
                }

                else {
                    for (thetaIdx in 0 until slices) {
                        // Any other value for phiIdx is considered to be part of the strip triangles
                        val nextBottomIdx = if (thetaIdx == slices - 1) { offset - (slices-1) } else { offset + 1 }
                        val nextTopIdx = if (thetaIdx == slices - 1) { offset + 1 } else { offset + (slices+1) }
                        val bottomTriangle = Face(offset, offset + slices, nextBottomIdx)
                        val topTriangle = Face(nextBottomIdx, offset + slices, nextTopIdx)
                        faces.add(bottomTriangle)
                        faces.add(topTriangle)
                        offset ++
                    }
                }

            }

            return Mesh(vertices, faces)

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