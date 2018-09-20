package com.example.test.project.widget

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ECGChartHelper(private val lineChart: LineChart?) {

  private val MAX_VISIBLE_COUNT = 1100

  init {
    lineChart?.run {
      description.isEnabled = false
      setDrawGridBackground(false)

      setTouchEnabled(true)
      isDragEnabled = true
      setScaleEnabled(false)
      setPinchZoom(false)

      axisLeft.axisMaximum = 4500f
      axisLeft.axisMinimum = -100f
      axisLeft.isEnabled = false
      axisRight.isEnabled = false

      setHardwareAccelerationEnabled(true)

      xAxis.setDrawGridLines(false)
      axisLeft.setDrawGridLines(false)

      xAxis.isEnabled = false
      setViewPortOffsets(-10F, 0F, -10F, 0F)
      legend.isEnabled = false
      setMaxVisibleValueCount(MAX_VISIBLE_COUNT)

      data = LineData()
      postInvalidate()
    }
  }

  fun addEntry(value: DoubleArray) {
    lineChart?.data?.let { data ->
      var dataSet = data.getDataSetByIndex(0) as? LineDataSet

      if (dataSet == null) {
        dataSet = createDataSet()
        data.addDataSet(dataSet)
      }

      value.forEach {
        dataSet.addEntry(Entry(dataSet.entryCount.toFloat(), it.toFloat()))
        data.notifyDataChanged()
      }

      lineChart.notifyDataSetChanged()
      lineChart.setVisibleXRangeMaximum(MAX_VISIBLE_COUNT.toFloat())
      lineChart.moveViewToX(data.entryCount.toFloat() - 1f)
      lineChart.postInvalidate()
    }
  }

  private fun createDataSet(values: List<Entry>? = null): LineDataSet {
    val set = LineDataSet(values, "")
    set.axisDependency = YAxis.AxisDependency.LEFT
    set.color = Color.BLUE
    set.lineWidth = 1f
    set.setDrawCircles(false)
    set.setDrawValues(false)
    return set
  }
}