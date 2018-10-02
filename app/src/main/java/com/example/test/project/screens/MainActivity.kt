package com.example.test.project.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemReselectedListener
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.util.Log
import android.view.MenuItem
import band.mawi.android.bluetooth.model.BandLocation
import band.mawi.android.bluetooth.model.Battery
import band.mawi.android.bluetooth.model.ConnectionState
import band.mawi.android.bluetooth.model.Device
import band.mawi.android.bluetooth.model.fit.FitInfo
import band.mawi.android.bluetooth.model.fit.FitRecord
import band.mawi.android.bluetooth.model.fit.FitState
import band.mawi.android.bluetooth.model.motion.MotionPacket
import com.example.test.project.listeners.FitActionListener
import com.example.test.project.listeners.MotionActionListener
import com.example.test.project.listeners.OnECGActionListener
import com.example.test.project.listeners.SettingsActionListener
import com.example.test.project.screens.fragments.ActivityFragment
import com.example.test.project.screens.fragments.ECGFragment
import com.example.test.project.screens.fragments.MotionFragment
import com.example.test.project.screens.fragments.SettingsFragment
import com.example.test.project.screens.main.MainScreenContract
import com.example.test.project.screens.main.MainScreenPresenter
import com.example.test.project.R
import com.example.test.project.toMainThread
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.getStackTraceString
import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity(), OnNavigationItemSelectedListener, OnNavigationItemReselectedListener, MainScreenContract.View {

  override val TAG = this@MainActivity.javaClass.simpleName

  override var layoutId = R.layout.activity_main

  var onECGActionListener: OnECGActionListener? = null
  var onMotionActionListener: MotionActionListener? = null
  var onFitActionListener: FitActionListener? = null
  var onSettingsActionListener: SettingsActionListener? = null

  override var fragmentContainer = R.id.main_fragment_container

  private lateinit var presenter: MainScreenContract.Presenter

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = MainScreenPresenter(bluetoothClient)
    presenter.attachView(this)

    main_navigation.setOnNavigationItemReselectedListener(this)
    main_navigation.setOnNavigationItemSelectedListener(this)

    replaceFragment(ECGFragment()) { checkPermissions() }
    main_navigation.selectedItemId = R.id.action_ecg
  }

  override fun onNavigationItemReselected(item: MenuItem) {

  }

  @SuppressLint("CheckResult")
  override fun onBluetoothEnabled() {
    super.onBluetoothEnabled()
    presenter.onStartHeart()

    bluetoothClient.observeConnectionChanges()
        ?.filter { it == ConnectionState.DISCONNECTED }
        ?.take(1)
        ?.toMainThread()
        ?.subscribe({
          startActivity<SearchDevicesActivity>()
          finish()
        }, { showError(it) })
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_ecg -> replaceFragment(ECGFragment()) { presenter.onStartHeart() }
      R.id.action_activity -> replaceFragment(ActivityFragment()) { presenter.onStartFitService() }
      R.id.action_motion -> replaceFragment(MotionFragment()) { presenter.onStartGyroAxel() }
      R.id.action_settings -> replaceFragment(SettingsFragment()) { presenter.onStartDeviceManager() }
    }

    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.detachView()
  }

  fun setSamplingMode(index: Int) {
    presenter.setSamplingMode(index)
  }

  fun setBandLocation(index: Int) {
    presenter.setBandLocation(index)
  }

  fun getLastRecords() {
    presenter.getLastRecords()
  }

  fun disconnect() {
    presenter.disconnect()
  }

  override fun onPointsLoaded(values: DoubleArray) {
    onECGActionListener?.onDataLoaded(values)
  }

  override fun onSessionChanged(isAttached: Boolean) {
    onECGActionListener?.onSessionChanged(isAttached)
  }

  override fun onHeartRateChanged(heartRate: Int) {
    onECGActionListener?.onHeartRateChanged(heartRate)
  }

  override fun onMotionChanged(motionPacket: MotionPacket) {
    onMotionActionListener?.onMotionChanged(motionPacket)
  }

  override fun onFitStateChange(fitState: FitState) {
    onFitActionListener?.onDailyProgressChange(fitState.fitProgress)
  }

  override fun onFitInfoChange(fitInfo: FitInfo) {
    onFitActionListener?.onFitInfoChange(fitInfo)
  }

  override fun onRecordsLoaded(records: List<FitRecord>) {
    onFitActionListener?.onActivityRecordsLoaded(records)
  }

  override fun onDeviceInfoChange(device: Device) {
    onSettingsActionListener?.onDeviceInformationChange(device)
  }

  override fun onBatteryLevelChange(battery: Battery) {
    onSettingsActionListener?.onBatteryLevelChange(battery)
  }

  override fun onBandLocationChange(bandLocation: BandLocation) {
    onSettingsActionListener?.onBandLocationChange(bandLocation)
  }

  override fun showError(throwable: Throwable?) {
    Log.e(TAG, throwable?.getStackTraceString())
  }
}