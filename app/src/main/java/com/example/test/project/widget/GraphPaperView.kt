package com.example.test.project.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class GraphPaperView : View {

  private var MILLIMETER = 20f
  private var BASE_LINE_WIDTH = 1f
  private var CORNER_LINE_WIDTH = 2f
  private var SMALL_LINE_WIDTH = 1f
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val bounds = Rect()

  private var countOfVerticalLines = 0
  private var countOfHorizontalLines = 0

  constructor(context: Context?) : super(context) { init() }
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init() }
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

  private fun init() {
    MILLIMETER = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, resources.displayMetrics)
    paint.apply {
      color = Color.parseColor("#F8B1C8")
      style = Paint.Style.STROKE
    }
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    canvas?.getClipBounds(bounds)
    countOfHorizontalLines = (bounds.height() / MILLIMETER).toInt()
    countOfVerticalLines = (bounds.width() / MILLIMETER).toInt()
    canvas?.let { drawLines(it) }
  }

  private fun drawLines(canvas: Canvas) {
    for (index in 0 until countOfHorizontalLines) {
      when {
        index == 0 || index == countOfHorizontalLines - 1 -> paint.strokeWidth = BASE_LINE_WIDTH
        index % 5 == 0 -> paint.strokeWidth = CORNER_LINE_WIDTH
        else -> paint.strokeWidth = SMALL_LINE_WIDTH
      }

      canvas.drawLine(0f, index * MILLIMETER, bounds.width().toFloat(), index * MILLIMETER, paint)
    }

    for (index in 0 until countOfVerticalLines) {
      when {
        index == 0 && index == countOfHorizontalLines - 1 -> paint.strokeWidth = BASE_LINE_WIDTH
        index % 5 == 0 -> paint.strokeWidth = CORNER_LINE_WIDTH
        else -> paint.strokeWidth = SMALL_LINE_WIDTH
      }

      canvas.drawLine(index * MILLIMETER, 0f, index * MILLIMETER, bounds.height().toFloat(), paint)
    }
  }
}