package com.al10101.android.soleil.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.al10101.android.soleil.utils.CONTEXT_EXT_TAG
import com.al10101.android.soleil.utils.OPENGL_ZERO
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException

fun Context.readTextFileFromResource(resourceId: Int): String {

    val body = StringBuilder()

    try {

        val inputStream: InputStream = resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var nextLine = bufferedReader.readLine()
        while (nextLine != null) {
            body.append(nextLine)
            body.append("\n")
            nextLine = bufferedReader.readLine()
        }

    } catch (e: IOException) {
        throw RuntimeException("Could not open resource: $resourceId", e)
    } catch (nfe: Resources.NotFoundException) {
        throw RuntimeException("Resource not found: $resourceId", nfe)
    }

    return body.toString()

}

fun Context.loadTexture(resourceId: Int): Int {

    // Define texture ID
    val textureObjectsId = IntArray(1)
    GLES20.glGenTextures(1, textureObjectsId, 0)

    if (textureObjectsId[0] == OPENGL_ZERO) {
        Log.v(CONTEXT_EXT_TAG, "Could not generate a new OpenGL texture object")
        return OPENGL_ZERO
    }

    // Define inScaled false because we want the original image
    val options = BitmapFactory.Options()
    options.inScaled = false

    // Do the actual decode, passing the Android context, resource ID and decoding options
    val bitmap = BitmapFactory.decodeResource(resources, resourceId, options)
    if (bitmap == null) {
        Log.v(CONTEXT_EXT_TAG, "Resource ID $resourceId could not be decoded")
        GLES20.glDeleteTextures(1, textureObjectsId, 0)
        return OPENGL_ZERO
    }

    // The future texture calls should be applied to this texture object. First, we tell OpenGL that
    // this should be treated as two-dimensional texture and the second parameter tells OpenGL which
    // texture object ID to bind to
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectsId[0])

    // Texture filtering
    GLES20.glTexParameteri(
        GLES20.GL_TEXTURE_2D,
        GLES20.GL_TEXTURE_MIN_FILTER,
        GLES20.GL_LINEAR_MIPMAP_LINEAR
    )
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    // Load the bitmap data into OpenGL
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    // Release garbage data
    bitmap.recycle()

    // Generate mipmap
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

    // Now that we have the texture loaded, a good practice is to then unbind from the texture
    // to avoid making further changes
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    // Return the texture ID object
    return textureObjectsId[0]

}