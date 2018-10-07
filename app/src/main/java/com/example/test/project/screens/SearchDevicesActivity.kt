package com.example.test.project.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import band.mawi.android.bluetooth.model.ConnectionState
import band.mawi.android.bluetooth.repository.ConnectivityListener
import com.example.test.project.adapter.DevicesAdapter
import com.example.test.project.toMainThread
import com.example.test.project.R
import band.mawi.android.bluetooth.model.ScanResult
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_search_devices.*
import org.jetbrains.anko.getStackTraceString
import org.jetbrains.anko.startActivity

class SearchDevicesActivity : BaseActivity(), DevicesAdapter.OnDeviceClickListener, ConnectivityListener {

  override val TAG = this@SearchDevicesActivity.javaClass.simpleName

  override var layoutId = R.layout.activity_search_devices

  private lateinit var adapter: DevicesAdapter

  private val scanTrigger = PublishSubject.create<Boolean>()

  private val connectionChangesSubject = PublishSubject.create<Boolean>()
  private val connectionChangesTrigger = PublishSubject.create<Boolean>()

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    search_device_toolbar.setupToolbar("Search devices")

    adapter = DevicesAdapter(this)
    search_devices_list.layoutManager = LinearLayoutManager(this)
    search_devices_list.adapter = adapter

    bluetoothClient.setConnectivityListener(this)
    checkPermissions()
  }

  override fun onBluetoothEnabled() {
    super.onBluetoothEnabled()
    subscribeOnEvents()
  }

  @SuppressLint("CheckResult")
  private fun subscribeOnEvents() {
    bluetoothClient.scanDevices()
        ?.takeUntil(scanTrigger)
        ?.toMainThread()
        ?.subscribe({ adapter.add(it) }, { it.onError() })

    connectionChangesSubject
        .flatMap { bluetoothClient.observeConnectionChanges() }
        .takeUntil(connectionChangesTrigger)
        .toMainThread()
        .subscribe({ switchResult(it) }, { it.onError() })
  }

  private fun switchResult(it: ConnectionState?) {
    when (it) {
      ConnectionState.CONNECTED -> {
        hideDialog()
        startActivity<MainActivity>()
        finish()
      }
      ConnectionState.CONNECTING -> {
        showDialog()
      }
      ConnectionState.DISCONNECTED -> {
        hideDialog()
      }
      ConnectionState.DISCONNECTING -> {
        connectionChangesTrigger.onNext(true)
        hideDialog()
      }
      else -> {
        // null
      }
    }
  }

  override fun onConnectionError(throwable: Throwable?) {
    throwable?.onError()
  }

  override fun onConnected() {
    // do something
  }

  private fun Throwable.onError() {
    Log.e(TAG, getStackTraceString())
  }

  override fun onDestroy() {
    super.onDestroy()
    connectionChangesTrigger.onNext(true)
  }

  @SuppressLint("CheckResult")
  override fun onDeviceClick(scanResult: ScanResult) {
    scanTrigger.onNext(true)
    bluetoothClient.connect(scanResult.bluetoothDevice.address)
    connectionChangesSubject.onNext(true)
  }
}