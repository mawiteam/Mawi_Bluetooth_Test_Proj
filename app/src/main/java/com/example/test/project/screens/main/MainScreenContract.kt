package com.example.test.project.screens.main

import band.mawi.android.bluetooth.model.BandLocation
import band.mawi.android.bluetooth.model.Battery
import band.mawi.android.bluetooth.model.Device
import band.mawi.android.bluetooth.model.fit.FitInfo
import band.mawi.android.bluetooth.model.fit.FitRecord
import band.mawi.android.bluetooth.model.fit.FitState
import band.mawi.android.bluetooth.model.motion.MotionPacket

object MainScreenContract {

  interface View {

    // ecg
    fun onPointsLoaded(values: DoubleArray)
    fun onSessionChanged(isAttached: Boolean)
    fun onHeartRateChanged(heartRate: Int)

    // motion
    fun onMotionChanged(motionPacket: MotionPacket)

    // fit
    fun onFitStateChange(fitState: FitState)
    fun onFitInfoChange(fitInfo: FitInfo)
    fun onRecordsLoaded(records: List<FitRecord>)

    // settings
    fun onDeviceInfoChange(device: Device)
    fun onBatteryLevelChange(battery: Battery)
    fun onBandLocationChange(bandLocation: BandLocation)

    fun showError(throwable: Throwable?)
  }

  interface Presenter {
    fun attachView(view: View)
    fun detachView()

    fun onStartHeart()

    fun onStartGyroAxel()
    fun setSamplingMode(index: Int)

    fun setBandLocation(index: Int)
    fun onStartDeviceManager()

    fun onStartFitService()

    fun getLastRecords()

    fun disconnect()
  }
}