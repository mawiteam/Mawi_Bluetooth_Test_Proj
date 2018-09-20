package com.example.test.project.screens.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import band.mawi.android.bluetooth.model.fit.FitInfo
import band.mawi.android.bluetooth.model.fit.FitProgress
import band.mawi.android.bluetooth.model.fit.FitRecord
import com.example.test.project.R
import com.example.test.project.adapter.FitRecordsAdapter
import com.example.test.project.listeners.FitActionListener
import kotlinx.android.synthetic.main.fragment_activity.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class ActivityFragment : BaseFragment(), FitActionListener {

  override var layoutId = R.layout.fragment_activity

  private lateinit var adapter: FitRecordsAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    adapter = FitRecordsAdapter(activity)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    currentActivity()?.onFitActionListener = this
    activity_records_list.layoutManager = LinearLayoutManager(activity)
    activity_records_list.adapter = adapter

    activity_get_last_records.onClick { currentActivity()?.getLastRecords() }
  }

  override fun onDailyProgressChange(daily: FitProgress) {
    fit_daily_active_time_progress.text = resources.getString(R.string.daily_active_time, daily.activeTime)
    fit_daily_calories_progress.text = resources.getString(R.string.daily_calories, daily.calories)
    fit_daily_steps_progress.text = resources.getString(R.string.daily_steps_progress, daily.steps)
    fit_daily_distance_progress.text = resources.getString(R.string.daily_distance, daily.distance)
  }

  override fun onFitInfoChange(info: FitInfo) {
    fit_history_record_count.text = resources.getString(R.string.fit_history_record_count, info.recordsCount)
  }

  override fun onActivityRecordsLoaded(records: List<FitRecord>) {
    adapter.addAll(records)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    currentActivity()?.onFitActionListener = null
  }
}