package com.al10101.android.soleil.models

import android.opengl.GLES20.*
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.RGB
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.data.VertexArray
import com.al10101.android.soleil.extensions.toVector
import com.al10101.android.soleil.extensions.transform
import com.al10101.android.soleil.programs.ShaderProgram
import com.al10101.android.soleil.utils.BYTES_PER_FLOAT
import java.nio.IntBuffer

const val POSITION_COMPONENT_COUNT = 3
const val COLOR_COMPONENT_COUNT = 4
const val NORMAL_COMPONENT_COUNT = 3
const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
const val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT +
        COLOR_COMPONENT_COUNT +
        NORMAL_COMPONENT_COUNT +
        TEXTURE_COORDINATES_COMPONENT_COUNT
const val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

class Mesh(meshContainer: MeshContainer) {

    private val nTotalElements = meshContainer.faces.size * 3
    private val vertexArray = VertexArray(meshContainer.vertexData, meshContainer.faces)

    fun bindData(program: ShaderProgram) {
        var offset = 0

        vertexArray.setVertexAttribPointer(
            offset, program.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        offset += POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT

        vertexArray.setVertexAttribPointer(
            offset, program.aColorLocation,
            COLOR_COMPONENT_COUNT, STRIDE
        )
        offset += COLOR_COMPONENT_COUNT * BYTES_PER_FLOAT

        vertexArray.setVertexAttribPointer(
            offset, program.aNormalLocation,
            NORMAL_COMPONENT_COUNT, STRIDE
        )
        offset += NORMAL_COMPONENT_COUNT * BYTES_PER_FLOAT

        vertexArray.setVertexAttribPointer(
            offset, program.aTextureCoordinatesLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE
        )

    }

    fun draw() {
        drawIndexedElements()
    }

    private fun drawIndexedElements() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexArray.ibo)
        glDrawElements(GL_TRIANGLES, nTotalElements, GL_UNSIGNED_SHORT, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

}
