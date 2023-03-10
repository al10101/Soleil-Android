package com.al10101.android.soleil.data

import android.opengl.GLES20.*
import com.al10101.android.soleil.models.Face
import com.al10101.android.soleil.utils.BYTES_PER_FLOAT
import com.al10101.android.soleil.utils.BYTES_PER_INT
import com.al10101.android.soleil.utils.BYTES_PER_SHORT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class VertexArray(vertexData: FloatArray, faces: List<Face>) {

    private val vbo: Int
    val ibo: Int // It is not private because the Mesh.draw() method needs it

    init {
        var offset = 0
        val facesIndices = ShortArray(faces.size * 3)
        faces.forEach {
            facesIndices[offset++] = it.a
            facesIndices[offset++] = it.b
            facesIndices[offset++] = it.c
        }
        // Fill native memory with vertex and index elements
        val vertexBuffer = ByteBuffer
            .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertexData)
                position(0)
            }
        val indexBuffer = ByteBuffer
            .allocateDirect(facesIndices.size * BYTES_PER_SHORT)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer().apply {
                put(facesIndices)
                position(0)
            }
        // Allocate buffers
        val buffers = IntArray(2)
        glGenBuffers(2, buffers, 0)
        vbo = buffers[0]
        ibo = buffers[1]
        // Bind buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        // Transfer data from native memory to GPU buffer
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GL_STATIC_DRAW)
        // Unbind from the buffer after adding it
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        // Bind buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)
        // Transfer data from native memory to GPU buffer
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT, indexBuffer, GL_STATIC_DRAW)
        // Unbind from the buffer after adding it
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun setVertexAttribPointer(dataOffset: Int, attributeLocation: Int, componentCount: Int, stride: Int) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glEnableVertexAttribArray(attributeLocation)
        glVertexAttribPointer(
            attributeLocation,
            componentCount,
            GL_FLOAT,
            false,
            stride,
            dataOffset
        )
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

}