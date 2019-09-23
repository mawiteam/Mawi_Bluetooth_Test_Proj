package com.example.test.project

import android.app.Application
import band.mawi.android.bluetooth.MawiBluetooth
import com.example.test.project.ContextProvider

class SampleApp : Application() {

  override fun onCreate() {
    super.onCreate()

    ContextProvider.initialize(applicationContext)

    MawiBluetooth.initialize(this)
  }
}