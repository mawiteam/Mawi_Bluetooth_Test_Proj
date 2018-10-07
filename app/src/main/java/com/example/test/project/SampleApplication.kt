package com.example.test.project

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import band.mawi.android.bluetooth.source.MawiBluetoothClient

class SampleApplication : Application() {

  companion object {
    lateinit var client: MawiBluetoothClient
  }

  override fun onCreate() {
    super.onCreate()

    client = MawiBluetoothClient.initialize(this)
  }

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }
}