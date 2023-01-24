package com.al10101.android.soleil.custom

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

open class TouchableGLView(context: Context): GLSurfaceView(context) {

    private enum class GestureTypes {
        NONE, DRAG, ZOOM
    }

    private var gesture = GestureTypes.NONE
    private var firstPointerId: Int = 0
    private var secondPointerId: Int = 0

    private lateinit var touchableGLRenderer: TouchableGLRenderer

    override fun setRenderer(renderer: Renderer?) {
        if (renderer is TouchableGLRenderer) {
            touchableGLRenderer = renderer
            super.setRenderer(touchableGLRenderer)
        } else {
            throw RuntimeException("SoleilGLView can only render SoleilGLRenderer")
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        // Variable to save if the event has been consumed. Only false when ev == null or
        // the zoom cannot be made
        var consumed = true

        val normalizedX = (ev.x / width) * 2f - 1f
        val normalizedY = -((ev.y / height) * 2f - 1f)

        when (ev.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                gesture = GestureTypes.DRAG
                firstPointerId = ev.getPointerId(0)
                queueEvent {
                    touchableGLRenderer.handleTouchPress(normalizedX, normalizedY)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                gesture = GestureTypes.ZOOM
                secondPointerId = ev.getPointerId(ev.actionIndex)
                queueEvent {
                    touchableGLRenderer.handleZoomPress(ev, firstPointerId, secondPointerId)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (gesture == GestureTypes.DRAG) {
                    queueEvent {
                        touchableGLRenderer.handleTouchDrag(normalizedX, normalizedY)
                    }
                } else if (gesture == GestureTypes.ZOOM) {
                    Log.d("CursedRoomRenderer", "onTouchEvent: firstPointer= $firstPointerId  secondPointer= $secondPointerId")
                    queueEvent {
                        consumed = touchableGLRenderer.handleZoomCamera(ev, firstPointerId, secondPointerId)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                gesture = GestureTypes.NONE
                queueEvent {
                    touchableGLRenderer.stopMovement(ev)
                }
            }

        }

        return consumed

    }

    fun onDragModeChanged(dragMode: DragMode) {
        queueEvent {
            touchableGLRenderer.onDragModeChanged(dragMode)
        }
    }

    fun onZoomModeChanged(zoomMode: ZoomMode) {
        queueEvent {
            touchableGLRenderer.onZoomModeChanged(zoomMode)
        }
    }

}
