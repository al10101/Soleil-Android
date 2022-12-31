package com.al10101.android.soleil.programs

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1fv
import com.al10101.android.soleil.R
import com.al10101.android.soleil.uniforms.Uniforms

private const val U_KERNEL = "u_Kernel"

enum class KernelType {
    PSYCHEDELIC,
    GAUSSIAN_BLUR,
    EDGE_DETECTION
}

class KernelShaderProgram(
    context: Context,
    private val kernel: FloatArray
): ShaderProgram(
    context,
    R.raw.simple_texture_vs,
    R.raw.kernel_fs
) {

    private val uKernelLocation by lazy {
        glGetUniformLocation(program, U_KERNEL)
    }

    override fun setUniforms(uniforms: Uniforms) {
        super.setUniforms(uniforms)
        // Set kernel, a 3x3 matrix
        glUniform1fv(uKernelLocation, 9, kernel, 0)
    }

    companion object {
        fun defaultKernel(context: Context, kernelType: KernelType): KernelShaderProgram {
            val kernel = FloatArray(9)
            when (kernelType) {
                KernelType.PSYCHEDELIC -> {
                    kernel[0] = -1f; kernel[1] = -1f; kernel[2] = -1f
                    kernel[3] = -1f; kernel[4] =  9f; kernel[5] = -1f
                    kernel[6] = -1f; kernel[7] = -1f; kernel[8] = -1f
                }
                KernelType.GAUSSIAN_BLUR -> {
                    kernel[0] = 1f / 16f; kernel[1] = 2f / 16f; kernel[2] = 1f / 16f
                    kernel[3] = 2f / 16f; kernel[4] = 4f / 16f; kernel[5] = 2f / 16f
                    kernel[6] = 1f / 16f; kernel[7] = 2f / 16f; kernel[8] = 1f / 16f
                }
                KernelType.EDGE_DETECTION -> {
                    kernel[0] = 1f; kernel[1] = 1f; kernel[2] = 1f
                    kernel[3] = 1f; kernel[4] =-8f; kernel[5] = 1f
                    kernel[6] = 1f; kernel[7] = 1f; kernel[8] = 1f
                }
            }
            return KernelShaderProgram(context, kernel)
        }
    }

}