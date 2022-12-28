package com.al10101.android.soleil.models

import android.opengl.GLES20.*
import com.al10101.android.soleil.data.VertexArray
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
    // As default, assume it renders with strip mode
    vertexData: FloatArray,
    private val nTotalElements: Int
) {

    private val vertexArray = VertexArray(vertexData)
    private var vertexIndices: IntBuffer? = null
    // The vertex indices is not needed if it renders in strip
    private var drawFun = { drawStripTriangles() }

    // As first secondary constructor, assume it renders with index mode.
    // This mode is actually more common, but it cannot be used as default
    // because it is a little more complex
    constructor(
        vertexData: FloatArray,
        faces: List<Face>
    ): this(vertexData, faces.size * 3) {

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

        drawFun = { drawIndexedTriangles() }

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
        drawFun()
    }

    private fun drawIndexedTriangles() {
        // nTotalElements here is the number of faces multiplied by 3
        glDrawElements(GL_TRIANGLES, nTotalElements, GL_UNSIGNED_INT, vertexIndices!!)
    }

    private fun drawStripTriangles() {
        // nTotalElements here is the number of fan elements initialized in the constructor
        glDrawArrays(GL_TRIANGLE_STRIP, 0, nTotalElements)
    }

}
