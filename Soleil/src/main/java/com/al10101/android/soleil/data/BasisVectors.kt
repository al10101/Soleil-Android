package com.al10101.android.soleil.data

import android.opengl.Matrix
import com.al10101.android.soleil.extensions.toVector

data class BasisVectors(
    var F: Vector, // Front
    var U: Vector, // Up
    var V: Vector // Orthogonal to the others
) {

    fun newBasePointingTo(Fp: Vector): BasisVectors {

        val FpFpT = floatArrayOf(
            1f-Fp.x * Fp.x,   -Fp.x * Fp.y,   -Fp.x * Fp.z, 0f,
              -Fp.y * Fp.x, 1f-Fp.y * Fp.y,   -Fp.y * Fp.z, 0f,
              -Fp.z * Fp.x,   -Fp.z * Fp.y, 1f-Fp.z * Fp.z, 0f,
            0f, 0f, 0f, 1f
        )
        val up = FloatArray(4)
        Matrix.multiplyMV(up, 0, FpFpT, 0, floatArrayOf(U.x, U.y, U.z, 1f), 0)
        val Up = up.toVector().normalize()

        val Vp = Fp.cross(Up)

        return BasisVectors(Fp, Up, Vp)

    }

    /*
    fun fromRpyToAngle(Fp: Vector, Up: Vector, Vp: Vector): FloatArray {

        val R1 = floatArrayOf(
            F.x, U.x, V.x, 0f,
            F.y, U.y, V.y, 0f,
            F.z, U.z, V.z, 0f,
            0f, 0f, 0f, 1f
        )

        val R1T = FloatArray(16)
        transposeM(R1T, 0, R1, 0)

        val R2 = floatArrayOf(
            Fp.x, Up.x, Vp.x, 0f,
            Fp.y, Up.y, Vp.y, 0f,
            Fp.z, Up.z, Vp.z, 0f,
            0f, 0f, 0f, 1f
        )

        val Rp = FloatArray(16)
        multiplyMM(Rp, 0, R2, 0, R1T, 0)

        val thetaV = atan2(Rp[0], Rp[1]) * RADIANS_TO_DEGREES
        val thetaF = atan2(Rp[10], Rp[6]) * RADIANS_TO_DEGREES
        val thetaU = asin(Rp[2]) * RADIANS_TO_DEGREES

        return floatArrayOf(thetaV, thetaF, thetaU)

    }

    fun fromAngleToRotationMatrix(thetaVFU: FloatArray): FloatArray {

        val (thetaV, thetaF, thetaU) = thetaVFU

        val rotationMatrix = FloatArray(16).apply { identity() }
        rotateM(rotationMatrix, 0, thetaF, 0f, 0f, 1f)//F.x, F.y, F.z)
        rotateM(rotationMatrix, 0, thetaV, 1f, 0f, 0f)//V.x, V.y, V.z)
        rotateM(rotationMatrix, 0, thetaU, 0f, 1f, 0f)//U.x, U.y, U.z)

        return rotationMatrix

    }
    */

}