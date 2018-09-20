package com.example.test.project

import android.app.Application
import band.mawi.android.bluetooth.source.MawiBluetoothClient

class SampleApplication : Application() {

  companion object {
    lateinit var client: MawiBluetoothClient
  }

  override fun onCreate() {
    super.onCreate()

    client = MawiBluetoothClient.initialize(this)
  }
}