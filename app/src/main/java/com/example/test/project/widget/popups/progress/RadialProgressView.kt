package com.example.test.project.widget.popups.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.example.test.project.widget.color
import com.example.test.project.widget.dip
import com.example.test.project.R

class RadialProgressView(context: Context) : View(context) {

    private var lastUpdateTime: Long = 0
    private var radOffset: Float = 0.toFloat()
    private var currentCircleLength: Float = 0.toFloat()
    private var risingCircleLength: Boolean = false
    private var currentProgressTime: Float = 0.toFloat()
    private val cicleRect = RectF()

    private var progressColor: Int = 0

    private val decelerateInterpolator: DecelerateInterpolator
    private val accelerateInterpolator: AccelerateInterpolator
    private val progressPaint: Paint
    private val rotationTime = 2000f
    private val risingTime = 500f
    private var size: Int = 0

    init {

        size = context.dip(40f)

        progressColor = color(R.color.colorPrimary)
        decelerateInterpolator = DecelerateInterpolator()
        accelerateInterpolator = AccelerateInterpolator()
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeWidth = context.dip(3f).toFloat()
        progressPaint.color = progressColor
    }

    private fun updateAnimation() {
        val newTime = System.currentTimeMillis()
        var dt = newTime - lastUpdateTime
        if (dt > 17) {
            dt = 17
        }
        lastUpdateTime = newTime

        radOffset += 360 * dt / rotationTime
        val count = (radOffset / 360).toInt()
        radOffset -= (count * 360).toFloat()

        currentProgressTime += dt.toFloat()

        if (currentProgressTime >= risingTime) {
            currentProgressTime = risingTime
        }

        currentCircleLength = if (risingCircleLength) {
            4 + 266 * accelerateInterpolator.getInterpolation(currentProgressTime / risingTime)
        } else {
            4 - 270 * (1.0f - decelerateInterpolator.getInterpolation(currentProgressTime / risingTime))
        }
        if (currentProgressTime == risingTime) {
            if (risingCircleLength) {
                radOffset += 270f
                currentCircleLength = -266f
            }
            risingCircleLength = !risingCircleLength
            currentProgressTime = 0f
        }
        invalidate()
    }

    fun setSize(value: Int) {
        size = value
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = progressColor
    }

    override fun onDraw(canvas: Canvas) {
        val x = (measuredWidth - size) / 2
        val y = (measuredHeight - size) / 2
        cicleRect.set(x.toFloat(), y.toFloat(), (x + size).toFloat(), (y + size).toFloat())
        canvas.drawArc(cicleRect, radOffset, currentCircleLength, false, progressPaint)
        updateAnimation()
    }
}
