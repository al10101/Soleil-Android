package com.al10101.android.soleil.custom

import android.opengl.GLSurfaceView
import android.opengl.Matrix.multiplyMV
import android.util.Log
import android.view.MotionEvent
import com.al10101.android.soleil.data.Plane
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Ray
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.divideByW
import com.al10101.android.soleil.extensions.identity
import com.al10101.android.soleil.extensions.rotation
import com.al10101.android.soleil.extensions.toVector
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.uniforms.Camera
import com.al10101.android.soleil.uniforms.Uniforms

interface TouchableGLRenderer: GLSurfaceView.Renderer {

    var zoomMode: ZoomModes
    var models: MutableList<Model>
    var controls: Controls
    var camera: Camera
    var uniforms: Uniforms
    var maxNorm: Float

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        // Define a plane representing the background
        val plane = Plane(Vector.zero, Vector(0f, 0f, maxNorm))
        // Find out where the touched point intersects plane
        val touchedPoint = ray.intersectionPointWith(plane)
        controls.previousTouch = Vector(touchedPoint.x, touchedPoint.y, maxNorm)
    }

    fun handleZoomPress(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int) {
        // If the event happens too fast and one of the fingers is not registered
        // correctly, the zoom operation must be cancelled
        if (ev.pointerCount == 1) { return }
        controls.oldDist = spacing(ev, firstPointerId, secondPointerId)
        // Define the mid point
        val firstTouchVector = retrieveEventAsVector(ev, firstPointerId)
        val secondTouchVector = retrieveEventAsVector(ev, secondPointerId)
        controls.midTouch = firstTouchVector.add(secondTouchVector).mul(0.5f)
        controls.startFov = camera.fovy
        controls.startPosZ = camera.position.z
        controls.previousTouch.x = controls.midTouch.x
        controls.previousTouch.y = controls.midTouch.y
        controls.currentTouch.x = controls.midTouch.x
        controls.currentTouch.y = controls.midTouch.y
    }

    fun handleTouchDragToRotate(normalizedX: Float, normalizedY: Float) {
        // Find out where the touched point intersects the plane
        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val plane = Plane(Vector.zero, Vector(0f, 0f, maxNorm))
        val touchedPoint = ray.intersectionPointWith(plane)
        // Define new vector
        controls.currentTouch = Vector(touchedPoint.x, touchedPoint.y, maxNorm)
        // Compute new rotation taking the previous touch as the reference point and
        // the current (most recent) touch as the direction of the rotation
        val quaternion = Quaternion(controls.previousTouch, controls.currentTouch)
        controls.rotationMatrix.rotation(quaternion)
    }

    fun handleZoomCamera(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int): Boolean {
        // If the event happens too fast and one of the fingers is not registered
        // correctly, the zoom operation must be cancelled
        if (ev.pointerCount == 1) { return false }
        // Boundaries for the operation
        val minFov = 5f
        val maxFov = 120f
        val minZ = camera.near + maxNorm * 0.5f
        val maxZ = camera.far - maxNorm
        val newDist = spacing(ev, firstPointerId, secondPointerId)
        val zoom = controls.oldDist / newDist
        controls.currentFov = controls.startFov * zoom
        controls.currentPosZ = controls.startPosZ * zoom
        controls.previousTouch.x = controls.midTouch.x
        controls.previousTouch.y = controls.midTouch.y
        return when (zoomMode) {
            ZoomModes.PERSPECTIVE ->
                if (controls.currentFov in minFov..maxFov) {
                    camera.fovy = controls.currentFov
                    setClipping()
                    true
                } else {
                    false
                }
            ZoomModes.TRANSLATION ->
                if (controls.currentPosZ in minZ..maxZ) {
                    camera.position.z = controls.currentPosZ
                    Log.d("CursedRoomRenderer", "handleZoomCamera: zoom=$zoom <${camera.position.z}) -> (${camera.center.z}))")
                    setClipping()
                    true
                } else {
                    false
                }
        }

    }

    fun stopMovement(ev: MotionEvent) {
        // Set the final rotation as the default model matrix for every model
        models.forEach { it.overwriteModelMatrix(controls.rotationMatrix) }
        // Set identity so it doesn't apply anymore
        controls.rotationMatrix.identity()
    }

    private fun setClipping() {
        // Compute all matrices
        camera.apply {
            setViewMatrix()
            setPerspectiveProjectionMatrix()
            setViewProjectionMatrix()
            invertViewProjectionMatrix()
        }
        uniforms.viewMatrix = camera.viewMatrix
        uniforms.projectionMatrix = camera.projectionMatrix
        uniforms.cameraPosition = camera.position
    }

    private fun convertNormalized2DPointToRay(normalizedX: Float, normalizedY: Float): Ray {

        val nearPointNdc = floatArrayOf(normalizedX, normalizedX, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        multiplyMV(nearPointWorld, 0, camera.invertedViewProjectionMatrix, 0, nearPointNdc, 0)
        multiplyMV(farPointWorld, 0, camera.invertedViewProjectionMatrix, 0, farPointNdc, 0)

        nearPointWorld.divideByW()
        farPointWorld.divideByW()

        val nearPointRay = nearPointWorld.toVector()
        val farPointRay = farPointWorld.toVector()

        return Ray(nearPointRay, farPointRay.sub(nearPointRay))

    }

    private fun spacing(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int): Float {
        // Get both touches
        val firstTouchVector = retrieveEventAsVector(ev, firstPointerId)
        val secondTouchVector = retrieveEventAsVector(ev, secondPointerId)
        val diff = firstTouchVector.sub(secondTouchVector)
        return diff.length()
    }

    private fun retrieveEventAsVector(ev: MotionEvent, pointerId: Int): Vector {
        val (touchEventX: Float, touchEventY: Float) = ev.findPointerIndex(pointerId).let {
            ev.getX(it) to ev.getY(it)
        }
        return Vector(touchEventX, touchEventY, 0f)
    }

}