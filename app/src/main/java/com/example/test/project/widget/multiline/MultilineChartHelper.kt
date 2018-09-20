package com.example.test.project.widget.multiline

import android.graphics.Color
import band.mawi.android.bluetooth.model.motion.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import org.jetbrains.anko.backgroundColor


class MultilineChartHelper(private val lineChart: LineChart?, private val isAxel: Boolean) {

  private val axelBuffer = RingEntryBuffer(3, 130, 0f)
  private val gyroBuffer = RingEntryBuffer(3, 130, 0f)

  private val titles = listOf("X", "Y", "Z")
  private val colors = listOf(ColorTemplate.VORDIPLOM_COLORS, ColorTemplate.COLORFUL_COLORS)

  private val MOTION_DEFAULT = MotionConfig(
      MotionSamplingMode.SAMPLING_26Hz,
      true, AxelFullscale.AXEL_FS_16G,
      true, GyroFullscale.GYRO_FS_500DPS)

  private val CHART_MARGIN_PERCENT = 10

  private val lineData = LineData()

  init {
    val scale = when {
      isAxel -> MOTION_DEFAULT.axelFullscale.fullScale
      else -> MOTION_DEFAULT.gyroFullscale.fullScale
    }

    configureChart(-getChartRange(scale), getChartRange(scale))
    lineChart?.data = lineData
  }

  private fun configureChart(bottom: Float, top: Float) {
    lineChart?.run {
      isHighlightPerDragEnabled = false
      description.isEnabled = false
      isDragEnabled = true
      setTouchEnabled(false)
      setScaleEnabled(true)

      dragDecelerationFrictionCoef = 0.95f
      backgroundColor = Color.WHITE

      axisLeft.axisMaximum = top
      axisLeft.axisMinimum = bottom
      axisLeft.setDrawGridLines(true)

      axisRight.isEnabled = false

      xAxis.setAvoidFirstLastClipping(true)
      xAxis.isEnabled = true
    }

    initChartData()
  }

  private fun getChartRange(scale: Int) = scale.toFloat() * (100f + CHART_MARGIN_PERCENT.toFloat()) / 100f

  fun updateData(packet: MotionPacket) {
    axelBuffer.pushSamples(packet.index,
        packet.axelSamples[0].x.toFloat(),
        packet.axelSamples[0].y.toFloat(),
        packet.axelSamples[0].z.toFloat(),
        packet.axelSamples[1].x.toFloat(),
        packet.axelSamples[1].y.toFloat(),
        packet.axelSamples[1].z.toFloat(),
        packet.axelSamples[2].x.toFloat(),
        packet.axelSamples[2].y.toFloat(),
        packet.axelSamples[2].z.toFloat())

    gyroBuffer.pushSamples(packet.index,
        packet.gyroSamples[0].x.toFloat(),
        packet.gyroSamples[0].y.toFloat(),
        packet.gyroSamples[0].z.toFloat(),
        packet.gyroSamples[1].x.toFloat(),
        packet.gyroSamples[1].y.toFloat(),
        packet.gyroSamples[1].z.toFloat(),
        packet.gyroSamples[2].x.toFloat(),
        packet.gyroSamples[2].y.toFloat(),
        packet.gyroSamples[2].z.toFloat())

    lineChart?.notifyDataSetChanged()
    lineChart?.invalidate()
  }

  private fun initChartData() {
    lineData.run {
      setValueTextColor(Color.LTGRAY)
      setValueTextSize(9f)
      var buffer = axelBuffer
      var colors = this@MultilineChartHelper.colors[0]

      when {
        !isAxel -> {
          buffer = gyroBuffer
          colors = this@MultilineChartHelper.colors[1]
        }
      }

      (0 until 3).forEach { addDataSet(getDataSet(titles[it], buffer.buffer[it], colors[it])) }
    }
  }

  private fun getDataSet(title: String, entries: List<Entry>, lineColor: Int): LineDataSet {
    val set = LineDataSet(entries, title)
    set.axisDependency = YAxis.AxisDependency.LEFT
    set.color = lineColor
    set.lineWidth = 1f
    set.setDrawCircles(false)
    set.setDrawValues(false)
    return set
  }
}