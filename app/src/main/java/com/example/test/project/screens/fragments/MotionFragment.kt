package com.example.test.project.screens.fragments

import android.os.Bundle
import android.view.View
import band.mawi.android.bluetooth.model.motion.MotionPacket
import com.example.test.project.R
import com.example.test.project.listeners.MotionActionListener
import com.example.test.project.screens.fragments.BaseFragment
import com.example.test.project.widget.multiline.MultilineChartHelper
import kotlinx.android.synthetic.main.fragment_gyro_axel.*
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener

class MotionFragment : BaseFragment(), MotionActionListener {

  override var layoutId = R.layout.fragment_gyro_axel

  private lateinit var gyroHelper: MultilineChartHelper
  private lateinit var axelHelper: MultilineChartHelper

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    currentActivity()?.onMotionActionListener = this
    gyroHelper = MultilineChartHelper(gyro_line_chart, false)
    axelHelper = MultilineChartHelper(axel_line_chart, true)

    currentActivity()?.setSamplingMode(0)

    sampling_modes.onItemSelectedListener {
      onItemSelected { _, _, i, _ ->
        currentActivity()?.setSamplingMode(i)
      }
    }
  }

  override fun onMotionChanged(motionPacket: MotionPacket) {
    axelHelper.updateData(motionPacket)
    gyroHelper.updateData(motionPacket)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    currentActivity()?.onMotionActionListener = null
  }
}