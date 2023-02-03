package com.al10101.android.soleil.custom

import android.opengl.GLSurfaceView
import android.opengl.Matrix.multiplyMV
import android.view.MotionEvent
import com.al10101.android.soleil.data.Plane
import com.al10101.android.soleil.data.Quaternion
import com.al10101.android.soleil.data.Ray
import com.al10101.android.soleil.data.Vector
import com.al10101.android.soleil.extensions.*
import com.al10101.android.soleil.models.Model
import com.al10101.android.soleil.uniforms.Camera
import com.al10101.android.soleil.uniforms.Uniforms

interface TouchableGLRenderer: GLSurfaceView.Renderer {

    var models: MutableList<Model>
    var controls: Controls
    var camera: Camera
    var uniforms: Uniforms
    var maxNorm: Float

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        // Define a plane representing the background
        val plane = Plane(Vector.zero, Vector.unitZ)
        // Find out where the touched point intersects plane
        val touchedPoint = ray.intersectionPointWith(plane)
        controls.previousTouch = Vector(touchedPoint.x, touchedPoint.y, maxNorm)
    }

    fun handleZoomPress(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int) {
        // If the event is not registered correctly, the zoom operation must be cancelled
        controls.oldDist = spacing(ev, firstPointerId, secondPointerId) ?: return
        // Define the mid point
        val firstTouchVector = retrieveEventAsVector(ev, firstPointerId) ?: return
        val secondTouchVector = retrieveEventAsVector(ev, secondPointerId) ?: return
        controls.midTouch = firstTouchVector.add(secondTouchVector).mul(0.5f)
        controls.startFov = camera.fovy
        controls.startPosZ = camera.position.z
        controls.previousTouch.x = controls.midTouch.x
        controls.previousTouch.y = controls.midTouch.y
        controls.currentTouch.x = controls.midTouch.x
        controls.currentTouch.y = controls.midTouch.y
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        // Find out where the touched point intersects the plane
        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val plane = Plane(Vector.zero, Vector.unitZ)
        val touchedPoint = ray.intersectionPointWith(plane)
        // Define new vector
        controls.currentTouch = Vector(touchedPoint.x, touchedPoint.y, maxNorm)
        when (controls.dragMode) {
            DragMode.ROTATION -> {
                // Compute new rotation taking the previous touch as the reference point and
                // the current (most recent) touch as the direction of the rotation
                val quaternion = Quaternion(controls.previousTouch, controls.currentTouch)
                controls.dragMatrix.rotation(quaternion)
            }
            DragMode.TRANSLATION -> {
                // Compute the new position, without getting out of the max norm
                val scaleNorm = 1.5f
                val deltaPosition = controls.currentTouch.sub(controls.previousTouch)
                controls.dragMatrix.translation(deltaPosition)
            }
        }
    }

    fun handleZoomCamera(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int): Boolean {
        // Boundaries for the operation
        val minFov = 5f
        val maxFov = 150f
        val minZ = camera.near + maxNorm * 0.2f
        val maxZ = camera.far - maxNorm
        // If the event is not registered correctly, the zoom operation must be cancelled
        val newDist = spacing(ev, firstPointerId, secondPointerId) ?: return false
        val zoom = controls.oldDist / newDist
        controls.currentFov = controls.startFov * zoom
        controls.currentPosZ = controls.startPosZ * zoom
        controls.previousTouch.x = controls.midTouch.x
        controls.previousTouch.y = controls.midTouch.y
        return when (controls.zoomMode) {
            ZoomMode.PROJECTION ->
                if (controls.currentFov in minFov..maxFov) {
                    camera.fovy = controls.currentFov
                    setClipping()
                    true
                } else {
                    false
                }
            ZoomMode.POSITION ->
                if (controls.currentPosZ in minZ..maxZ) {
                    camera.position.z = controls.currentPosZ
                    setClipping()
                    true
                } else {
                    false
                }
        }

    }

    fun stopMovement(ev: MotionEvent) {
        // Set the final rotation as the default model matrix for every model
        models.forEach { it.overwriteModelMatrix(controls.dragMatrix) }
        // Set identity so it doesn't apply anymore
        controls.dragMatrix.identity()
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

    private fun spacing(ev: MotionEvent, firstPointerId: Int, secondPointerId: Int): Float? {
        // Get both touches. If a problem is found during the process, return null
        val firstTouchVector = retrieveEventAsVector(ev, firstPointerId) ?: return null
        val secondTouchVector = retrieveEventAsVector(ev, secondPointerId) ?: return null
        val diff = firstTouchVector.sub(secondTouchVector)
        return diff.length()
    }

    private fun retrieveEventAsVector(ev: MotionEvent, pointerId: Int): Vector? {
        val (touchEventX: Float, touchEventY: Float) = ev.findPointerIndex(pointerId).let {
            try {
                ev.getX(it) to ev.getY(it)
            } catch (e: IllegalArgumentException) {
                // If, for any reason, the pointer is not found in the event, return null
                return null
            }
        }
        return Vector(touchEventX, touchEventY, 0f)
    }

    fun onDragModeChanged(dragMode: DragMode) {
        controls.dragMode = dragMode
    }

    fun onZoomModeChanged(zoomMode: ZoomMode) {
        controls.zoomMode = zoomMode
    }

}