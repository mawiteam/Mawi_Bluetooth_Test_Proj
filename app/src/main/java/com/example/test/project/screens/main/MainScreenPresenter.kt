package com.example.test.project.screens.main

import android.annotation.SuppressLint
import android.util.Log
import band.mawi.android.bluetooth.helpers.DefaultConfig
import band.mawi.android.bluetooth.model.BandLocation
import band.mawi.android.bluetooth.model.fit.FitInfo
import band.mawi.android.bluetooth.model.fit.FitRecordHeader
import band.mawi.android.bluetooth.model.motion.AxelFullscale
import band.mawi.android.bluetooth.model.motion.GyroFullscale
import band.mawi.android.bluetooth.model.motion.MotionConfig
import band.mawi.android.bluetooth.model.motion.MotionSamplingMode
import band.mawi.android.bluetooth.source.MawiBluetoothClient
import com.example.test.project.toMainThread
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*

class MainScreenPresenter(private val client: MawiBluetoothClient) : MainScreenContract.Presenter {

  private val TAG = this@MainScreenPresenter.javaClass.simpleName

  private var view: MainScreenContract.View? = null

  private val configurations = listOf(
      MotionConfig(MotionSamplingMode.SAMPLING_OFF, false, AxelFullscale.AXEL_FS_2G, false, GyroFullscale.GYRO_FS_125DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_13Hz, true, AxelFullscale.AXEL_FS_2G, true, GyroFullscale.GYRO_FS_125DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_26Hz, true, AxelFullscale.AXEL_FS_2G, true, GyroFullscale.GYRO_FS_125DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_26Hz, true, AxelFullscale.AXEL_FS_4G, true, GyroFullscale.GYRO_FS_245DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_26Hz, true, AxelFullscale.AXEL_FS_8G, true, GyroFullscale.GYRO_FS_500DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_26Hz, true, AxelFullscale.AXEL_FS_16G, true, GyroFullscale.GYRO_FS_1000DPS),
      MotionConfig(MotionSamplingMode.SAMPLING_52Hz, true, AxelFullscale.AXEL_FS_2G, true, GyroFullscale.GYRO_FS_125DPS)
  )

  private val ecgSubject = PublishSubject.create<Boolean>()
  private val motionSubject = PublishSubject.create<Boolean>()
  private val fitSubject = PublishSubject.create<Boolean>()
  private val settingsSubject = PublishSubject.create<Boolean>()

  private val fitRequestSubject = BehaviorSubject.create<FitRecordHeader>()

  private val counts = ArrayDeque<Int>()
  private var startId = 1000

  override fun attachView(view: MainScreenContract.View) {
    this.view = view
  }

  override fun onStartHeart() {
    reset()
    client.ecgService()?.run {
      observeECGStream()
          ?.takeUntil(ecgSubject)
          ?._subscribe { view?.onPointsLoaded(it.points.map { it.toDouble() }.toDoubleArray()) }

      observeHeartRate()
          ?.takeUntil(ecgSubject)
          ?._subscribe { view?.onHeartRateChanged(it.heartRate) }

      observeSession()
          ?.takeUntil(ecgSubject)
          ?._subscribe { view?.onSessionChanged(it.isAttached) }
    }
  }

  override fun onStartGyroAxel() {
    reset()
    client.motionService()?.observeMotionData()
        ?.takeUntil(motionSubject)
        ?._subscribe { view?.onMotionChanged(it) }
  }

  override fun onStartFitService() {
    reset()

    client.fitService()?.run {
      Observable.merge(readFitState(), observeFitState())
          ?.takeUntil(fitSubject)
          ?._subscribe { it?.let { view?.onFitStateChange(it) } }

      Observable.merge(readFitInfo(), observeFitInfo())
          ?.takeUntil(fitSubject)
          ?._subscribe { it?.let { view?.onFitInfoChange(it) } }

      val gson = GsonBuilder().setLenient().serializeSpecialFloatingPointValues().create()

      observeActivityHistory()
          ?.takeUntil(fitSubject)
          ?._subscribe {
            Log.d(TAG, gson.toJson(it))
            it?.records?.let { view?.onRecordsLoaded(it) }
//            if (counts.isNotEmpty()) {
//              val count = counts.pop()
//              startId += if (counts.size == 0) Math.abs(MAX_FIT_RECORDS_BATCH_SIZE - count) + count
//              else count
//              fitRequestSubject.onNext(FitRecordHeader(count, startId))
//            }
          }

      fitRequestSubject.takeUntil(fitSubject)
          .flatMapCompletable {
            Log.d(TAG, Gson().toJson(it))
            Log.d(TAG, "current_count=${counts.size}")
            client.fitService()?.requestActivityHistory(it)
          }._subscribe { Log.d(TAG, "FIT_REQUESTED") }
    }
  }

  override fun disconnect() {
    client.disconnect()
  }

  override fun setBandLocation(index: Int) {
    client.deviceManager()?.setBandLocation(BandLocation.values()[index])
        ?._subscribe { }
  }

  override fun onStartDeviceManager() {
    reset()
    client.deviceManager()?.run {
      readDeviceInformation()
          ?.takeUntil(settingsSubject)
          ?._subscribe { it?.let { view?.onDeviceInfoChange(it) } }

      Observable.merge(readBatteryLevel(), observeBatteryLevel())
          ?.takeUntil(settingsSubject)
          ?._subscribe { it?.let { view?.onBatteryLevelChange(it) } }

      readBandLocation()
          ?.takeUntil(settingsSubject)
          ?._subscribe { it?.let { view?.onBandLocationChange(it) } }
    }
  }

  override fun getLastRecords() {
    client.fitService()?.readFitInfo()
        ?.takeUntil(fitSubject)
        ?._subscribe {
          Log.d(TAG, Gson().toJson(it))
          it?.let { loadHistory(it) }
        }

    fitSubject._subscribe {
      counts.clear()
      startId = 0
    }
  }

  private fun loadHistory(fitInfo: FitInfo) {
    generateDeque(fitInfo)
    fitRequestSubject.onNext(FitRecordHeader(counts.last, fitInfo.recordsCount - 1 - counts.last))

//    fitRequestSubject.onNext(FitRecordHeader(DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE, startId))
  }

  override fun setSamplingMode(index: Int) {
    client.motionService()?.setSamplingMode(configurations[index])
  }

  override fun detachView() {
    this.view = null
  }

  @SuppressLint("CheckResult")
  private fun <T> Observable<T>._subscribe(func: (T) -> Unit) {
    toMainThread().subscribe({ func(it) }, { view?.showError(it) })
  }

  @SuppressLint("CheckResult")
  private fun Completable._subscribe(func: () -> Unit) {
    toMainThread().subscribe({ func() }, { view?.showError(it) })
  }

  private fun reset() {
    motionSubject.onNext(true)
    ecgSubject.onNext(true)
    fitSubject.onNext(true)
    settingsSubject.onNext(true)
  }

  private fun generateDeque(fitInfo: FitInfo) {
    counts.clear()
    val count = fitInfo.recordsCount / DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE
    counts.addAll((0 until count).map { DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE })
    val diff = fitInfo.recordsCount - count * DefaultConfig.MAX_FIT_RECORDS_BATCH_SIZE
    counts.add(diff)
    Log.d(TAG, "count=${counts.size}")
  }
}