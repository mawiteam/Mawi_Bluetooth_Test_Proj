package com.example.test.project.listeners

import band.mawi.android.bluetooth.model.fit.FitInfo
import band.mawi.android.bluetooth.model.fit.FitProgress
import band.mawi.android.bluetooth.model.fit.FitRecord

interface FitActionListener {
  fun onDailyProgressChange(daily: FitProgress)
  fun onFitInfoChange(info: FitInfo)
  fun onActivityRecordsLoaded(records: List<FitRecord>)
}