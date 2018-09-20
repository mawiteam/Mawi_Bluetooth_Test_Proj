package com.example.test.project.listeners

import band.mawi.android.bluetooth.model.BandLocation
import band.mawi.android.bluetooth.model.Battery
import band.mawi.android.bluetooth.model.Device

interface SettingsActionListener {
  fun onBatteryLevelChange(battery: Battery)
  fun onBandLocationChange(location: BandLocation)
  fun onDeviceInformationChange(device: Device)
}