package com.example.test.project.widget.multiline

import com.github.mikephil.charting.data.Entry

class RingEntryBuffer(arrayCount: Int, private val depth: Int, private val initialValue: Float) {

  val buffer = arrayListOf<List<Entry>>()
  var marker = 0

  init {
    (0 until arrayCount).forEach {
      val samples = ArrayList<Entry>(depth)
      populate(samples, initialValue, depth)
      buffer.add(samples)
    }
  }

  private fun populate(list: ArrayList<Entry>, initialValue: Float, count: Int) {
    (0 until count).forEach {
      list.add(Entry(it.toFloat(), initialValue))
    }
  }

  private fun updateEntry(list: List<Entry>, index: Float, value: Float) {
    val offset = index.toInt() % depth
    list[offset].x = offset.toFloat()
    list[offset].y = value
  }

  fun pushSamples(sampleIndex: Int, vararg values: Float) {
    (0 until buffer.size).forEach {
      updateEntry(buffer[it], sampleIndex.toFloat(), values[it])
    }
    this.marker = sampleIndex
  }

  fun getValuesAt(index: Int): FloatArray {
    val values = FloatArray(buffer.size)
    (0 until buffer.size).forEach {
      values[it] = buffer[it][index % depth].y
    }

    return values
  }
}