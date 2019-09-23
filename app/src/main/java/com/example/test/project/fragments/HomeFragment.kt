package com.example.test.project.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import band.mawi.android.bluetooth.MawiBluetoothClient
import band.mawi.android.bluetooth.models.DeviceState.DEVICE_READY_FOR_USE
import band.mawi.android.bluetooth.models.Mode
import band.mawi.android.bluetooth.models.OperationResult
import com.example.test.project.di.AppInjector
import com.example.test.project.prefs.Prefs
import com.example.test.project.utils.ble.BleReadiness
import com.example.test.project.utils.id
import com.example.test.project.R
import com.example.test.project.utils.operationResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeFragment : BaseFragment() {

  companion object {

    private val TAG = HomeFragment::class.java.simpleName

    fun fragment(): HomeFragment {
      return HomeFragment()
    }
  }

  init {
    AppInjector.homeComponent?.inject(this)
  }

  @Inject
  lateinit var bluetoothClient: MawiBluetoothClient

  private val ecgSteamBtn: Button by id(R.id.btn_ecg_stream)
  private val tvDeviceName: TextView by id(R.id.tv_name)
  private val tvDeviceAddress: TextView by id(R.id.tv_address)
  private val btnFitnessHistory: Button by id(R.id.btn_fitness_history)
  private val tvCalories: TextView by id(R.id.tv_daily_calories)
  private val tvSteps: TextView by id(R.id.tv_daily_steps)
  private val btnClearData: Button by id(R.id.btn_btn_clear_data)
  private val btnDisconnect: Button by id(R.id.btn_disconnect)
  private val tvBatteryLevel: TextView by id(R.id.tv_battery_level)

  override val layoutId: Int
    get() = R.layout.fragment_root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    rootDispatcher.setTitle("Home")
    ecgSteamBtn.setOnClickListener {
      push(EcgStreamFragment.fragment())
    }

    btnFitnessHistory.setOnClickListener {
      push(ReadFitnessHistoryFragment.fragment())
    }

    btnClearData.setOnClickListener {
      bluetoothClient.clearData()
          .subscribe()
    }

    btnDisconnect.setOnClickListener {
      bluetoothClient.unpair()
      Prefs.device = null
      rootDispatcher.dispatchDeviceConnected()
    }
  }

  override fun onResume() {
    super.onResume()

    if (Prefs.device == null) {
//      rootDispatcher.dispatchDeviceConnected()
      return
    }

    Prefs.device()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          tvDeviceAddress.text = it.address
          tvDeviceName.text = it.name
          Log.i("HomeFragment", it.toString())
        }, {})
        .disposeOnPause()

    if (rootDispatcher.isReadyForBleInteraction()) {
      onBleReady()
      return
    }

    BleReadiness.listen { onBleReady() }.disposeOnPause()
  }

  private fun onBleReady() {
    bluetoothClient.connectionState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { state ->
          when (state) {
            DEVICE_READY_FOR_USE -> onDeviceReadyForUse()
            else -> {
              ecgSteamBtn.isEnabled = false
              btnFitnessHistory.isEnabled = false
              btnClearData.isEnabled = false
              btnDisconnect.isEnabled = false
            }
          }
        }
        .disposeOnPause()

    bluetoothClient.deviceInformation()
        .operationResult { deviceInformation ->
          Log.i("HomeFragment", deviceInformation.toString())
          Prefs.device = Prefs.device?.copy(
              manufacturer = deviceInformation.manufacturer,
              modelNumber = deviceInformation.modelNumber,
              hwRevision = deviceInformation.hwRevision,
              fwRevision = deviceInformation.fwRevision,
              serialNumber = deviceInformation.serialNumber
          )
        }
        .disposeOnPause()

    bluetoothClient.connect(Prefs.device!!.address)

  }

  @SuppressLint("SetTextI18n")
  private fun onDeviceReadyForUse() {
    ecgSteamBtn.isEnabled = true
    btnFitnessHistory.isEnabled = true
    btnClearData.isEnabled = true
    btnDisconnect.isEnabled = true

    bluetoothClient.fitnessController().dailyFitnessData()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .operationResult { data ->
          tvCalories.text = "${data.progress.calories} cal"
          tvSteps.text = "${data.progress.steps}"
        }
        .disposeOnPause()

    bluetoothClient.batteryLevel()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .operationResult { data ->
          tvBatteryLevel.text = when (data.mode) {
            Mode.STANDBY -> "${data.level}%"
            Mode.CHARGING -> "Charging..."
          }
        }
        .disposeOnPause()
  }

  override fun handleBack(): Boolean {
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    AppInjector.homeComponent = null
  }
}