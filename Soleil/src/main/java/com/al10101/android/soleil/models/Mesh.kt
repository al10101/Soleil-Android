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

class Mesh(private val vertexArray: VertexArray, faces: List<Face>) {

    private var nVertices = faces.size * 3
    private var vertexIndices = IntBuffer.allocate(nVertices)

    init {
        var offset = 0
        val facesIndices = IntArray(nVertices)
        faces.forEach {
            facesIndices[offset++] = it.a
            facesIndices[offset++] = it.b
            facesIndices[offset++] = it.c
        }
        vertexIndices.put(facesIndices)
        vertexIndices.position(0)
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

    fun drawIndexedTriangles() {
        glDrawElements(GL_TRIANGLES, nVertices, GL_UNSIGNED_INT, vertexIndices)
    }

    fun drawStripTriangles(totalElements: Int) {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, totalElements)
    }

}
