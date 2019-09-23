package com.example.test.project.di

import band.mawi.android.bluetooth.MawiBluetooth
import band.mawi.android.bluetooth.MawiBluetoothClient
import band.mawi.android.bluetooth.models.RetainMode
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

  @Provides
  @Singleton
  @JvmStatic
  fun providesMawiBluetoothClient(): MawiBluetoothClient {
    return MawiBluetooth.client(retainMode = RetainMode.RETAIN_DISCONNECT)
  }
}