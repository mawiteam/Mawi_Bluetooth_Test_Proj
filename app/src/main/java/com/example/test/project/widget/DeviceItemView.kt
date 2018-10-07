package com.example.test.project.widget

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.test.project.R
import band.mawi.android.bluetooth.model.ScanResult
import kotlinx.android.synthetic.main.view_device_item.view.*

class DeviceItemView : FrameLayout {

  constructor(context: Context?) : super(context) { init(context) }
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context) }
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

  private fun init(context: Context?) = View.inflate(context, R.layout.view_device_item, this)

  @SuppressLint("SetTextI18n")
  fun setData(scanResult: ScanResult) {
    device_item_name.text = "${scanResult.bluetoothDevice.name} (rssi=${scanResult.rssi}dBm)"
    device_item_mac_address.text = "${scanResult.bluetoothDevice.address} (${getBondState(scanResult.bluetoothDevice.bondState)})"
  }

  private fun getBondState(state: Int) = if (BluetoothDevice.BOND_BONDED == state) "BONDED" else "NOT BONDED"
}