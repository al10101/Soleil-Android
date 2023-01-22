package com.al10101.android.soleil.models

import android.opengl.GLES20.*
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.data.VertexArray
import com.al10101.android.soleil.extensions.toVector
import com.al10101.android.soleil.extensions.transform
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.utils.BYTES_PER_FLOAT
import java.nio.IntBuffer

private const val POSITION_COMPONENT_COUNT = 3
private const val COLOR_COMPONENT_COUNT = 4
private const val NORMAL_COMPONENT_COUNT = 3
private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
private const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        COLOR_COMPONENT_COUNT +
        NORMAL_COMPONENT_COUNT +
        TEXTURE_COORDINATES_COMPONENT_COUNT
private const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Mesh constructor(
    vertexData: FloatArray,
    private val nTotalElements: Int,
    // As default, assume it renders with strip mode
    var glPrimitivesMode: Int = GL_TRIANGLE_STRIP
) {

    private val nVertices = vertexData.size / TOTAL_COMPONENT_COUNT
    private val vertexArray = VertexArray(vertexData)
    // The vertex indices is not needed if it renders in strip or fan
    private var vertexIndices: IntBuffer? = null
    private var drawPrimitives = { drawContinuousArray() }

    // As first secondary constructor, assume it renders with index mode.
    // This mode is actually more common, but it cannot be used as default
    // because it is a little bit more complex
    constructor(
        vertexData: FloatArray,
        faces: List<Face>,
    ): this(vertexData, faces.size * 3, GL_TRIANGLES) {

        var offset = 0
        val facesIndices = IntArray(nTotalElements)
        faces.forEach {
            facesIndices[offset++] = it.a
            facesIndices[offset++] = it.b
            facesIndices[offset++] = it.c
        }

        // If the rendering is indexed, we need the indices
        vertexIndices = IntBuffer.allocate(nTotalElements).apply {
            put(facesIndices)
            position(0)
        }

        drawPrimitives = { drawIndexedElements() }

    }

    fun bindData(program: ShaderProgram) {
        var offset = 0

        vertexArray.setVertexAttribPointer(
            offset, program.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        offset += POSITION_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            offset, program.aColorLocation,
            COLOR_COMPONENT_COUNT, STRIDE
        )
        offset += COLOR_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            offset, program.aNormalLocation,
            NORMAL_COMPONENT_COUNT, STRIDE
        )
        offset += NORMAL_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            offset, program.aTextureCoordinatesLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE
        )

    }

    fun draw() {
        drawPrimitives()
    }

    private fun drawIndexedElements() {
        // nTotalElements here is the number of faces multiplied by 3
        glDrawElements(glPrimitivesMode, nTotalElements, GL_UNSIGNED_INT, vertexIndices!!)
    }

    private fun drawContinuousArray() {
        // nTotalElements here is the number of fan elements initialized in the constructor
        glDrawArrays(glPrimitivesMode, 0, nTotalElements)
    }

    private fun updateVertexArray(vertexData: FloatArray, start: Int, count: Int) {
        vertexArray.updateBuffer(vertexData, start, count)
    }

    private fun updateVertexArray(vertexData: FloatArray, start: Int) {
        vertexArray.updateBuffer(vertexData, start)
    }

    private fun readVertexArray(start: Int, count: Int): FloatArray {
        return vertexArray.readBuffer(start, count)
    }

    fun updatePositionAndNormal(position: Vector, modelMatrix: FloatArray, temp: FloatArray) {

        var transformedVector: Vector

        for (i in 0 until nVertices) {

            // Read the original value, transform and update the vertex array
            var start = i * TOTAL_COMPONENTS_COUNT
            val originalPosition = this.readVertexArray(start, POSITION_COMPONENT_COUNT)
            transformedVector = floatArrayOf(
                originalPosition[0],
                originalPosition[1],
                originalPosition[2], 1f
            ).apply { transform(modelMatrix, temp) }.toVector()
            val transformedPosition = floatArrayOf(
                transformedVector.x,
                transformedVector.y,
                transformedVector.z
            )
            this.updateVertexArray(transformedPosition, start)

            start += POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT
            val originalNormal = this.readVertexArray(start, NORMAL_COMPONENT_COUNT)
            transformedVector = floatArrayOf(
                originalNormal[0],
                originalNormal[1],
                originalNormal[2], 1f
            ).apply { transform(modelMatrix, temp) }.toVector()
                .sub(position).normalize()
            val transformedNormal = floatArrayOf(
                transformedVector.x,
                transformedVector.y,
                transformedVector.z
            )
            this.updateVertexArray(transformedNormal, start)

        }

    }

}
