package com.example.test.project.utils.ble

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import band.mawi.android.bluetooth.MawiBluetooth

object BleUtils {

  const val ENABLE_BLE_RC = 100

  fun checkBle(activity: AppCompatActivity, enabled: () -> Unit = {}) {
    if (MawiBluetooth.isBleEnabled) {
      enabled()
      return
    }

    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    activity.startActivityForResult(intent, ENABLE_BLE_RC)
  }
}