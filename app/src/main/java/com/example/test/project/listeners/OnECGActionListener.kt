package com.example.test.project.listeners

interface OnECGActionListener {
  fun onDataLoaded(data: DoubleArray)
  fun onSessionChanged(isAttached: Boolean)
  fun onHeartRateChanged(heartRate: Int)
}