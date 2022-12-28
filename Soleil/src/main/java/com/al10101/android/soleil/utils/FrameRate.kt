package com.al10101.android.soleil.utils

import android.os.SystemClock
import android.util.Log

object FrameRate {

    private var frameCount: Int = 0
    private var startTimeMs: Long = 0
    private var frameStartTimeMs: Long = 0

    fun limitFrameRate(framesPerSecond: Int) {
        val elapsedFrameTimeMs = SystemClock.elapsedRealtime() - frameStartTimeMs
        val expectedFrameTimeMs = 1000 / framesPerSecond
        val timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs
        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs)
        }
        frameStartTimeMs = SystemClock.elapsedRealtime()
    }

    fun logFrameRate(tag: String = FRAME_RATE_TAG) {
        val elapsedRealTimeMs = SystemClock.elapsedRealtime()
        val elapsedSeconds = (elapsedRealTimeMs - startTimeMs) / 1000
        if (elapsedSeconds >= 1.0) {
            Log.i(tag, "${frameCount / elapsedSeconds} fps")
            startTimeMs = SystemClock.elapsedRealtime()
            frameCount = 0
        }
        frameCount ++
    }

}