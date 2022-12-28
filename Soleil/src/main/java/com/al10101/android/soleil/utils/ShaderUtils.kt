package com.al10101.android.soleil.utils

import android.opengl.GLES20
import android.util.Log

object ShaderUtils {

    private fun loadShader(shaderType: Int, source: String): Int {

        val shader = GLES20.glCreateShader(shaderType)
        if (shader != OPENGL_ZERO) {

            // Create and compile shader
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)

            // Check for any errors
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)

            if (compiled[0] == OPENGL_ZERO) {
                Log.e(SHADER_UTILS_TAG, "Could not compile shader $shaderType:")
                Log.e(SHADER_UTILS_TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                return OPENGL_ZERO
            }

        }

        return shader

    }

    private fun validateProgram(program: Int) {

        // Do the actual validation
        GLES20.glValidateProgram(program)

        // Check the status
        val validateStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)

        val success = validateStatus[0] != OPENGL_ZERO
        if (!success) {
            Log.v(SHADER_UTILS_TAG, "Results of the validating program nr. $program: $success")
            Log.v(SHADER_UTILS_TAG, GLES20.glGetProgramInfoLog(program))
        }

    }

    fun buildProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {

        // Load both programs and return their respective handles
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Check if any errors occurred
        if (vertexShader == OPENGL_ZERO || fragmentShader == OPENGL_ZERO) {
            return OPENGL_ZERO
        }

        // Creates an empty program object to host the two shaders
        val program = GLES20.glCreateProgram()

        if (program != OPENGL_ZERO) {

            // Attach both shaders to the program
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)

            // Link the program to OpenGL pipeline
            GLES20.glLinkProgram(program)

            // Check for any possible errors
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(SHADER_UTILS_TAG, "Could not link program:")
                Log.e(SHADER_UTILS_TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                return OPENGL_ZERO
            }

        }

        validateProgram(program)

        return program

    }

}