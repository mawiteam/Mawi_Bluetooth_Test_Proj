package com.example.test.project.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import band.mawi.android.bluetooth.model.fit.FitRecord
import com.example.test.project.R
import kotlinx.android.synthetic.main.view_record_item.view.*

class RecordItemView : FrameLayout {

  constructor(context: Context?) : super(context) {
    init(context)
  }

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init(context)
  }

  private fun init(context: Context?) = View.inflate(context, R.layout.view_record_item, this)

  @SuppressLint("SetTextI18n")
  fun setData(record: FitRecord) {
    record.dateTime.run {
      record_item_date.text = "$year.$month.${day}_$hours:$minutes"
    }

    record_item_steps_count.text = "steps = ${record.steps}"
    record_item_activity_std.text = "activity_std = ${record.activityStd}"
  }
}