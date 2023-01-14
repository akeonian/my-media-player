package com.example.mymediaplayer.helper

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs

class FlingGestureHelper(
    private val view: View,
    private val flingLeft: () -> Unit = {},
    private val flingRight: () -> Unit = {},
    private val flingUp: () -> Unit = {},
    private val flingDown: () -> Unit = {}
) : GestureDetector.SimpleOnGestureListener() {

    fun attach() {
        val gestureDetector = GestureDetectorCompat(view.context, this)
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        view.performClick()
        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (abs(velocityX) > abs(velocityY)) {
            // Horizontal fling
            if (e1.x < e2.x) {
                // right fling
                flingRight()
            } else {
                // left fling
                flingLeft()
            }
        } else {
            // Vertical fling
            if (e1.y > e2.y) {
                // Up fling
                flingUp()
            } else {
                flingDown()
            }
        }
        return true
    }
}