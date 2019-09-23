package com.example.test.project.activities

interface RootDispatcher {

  fun dispatchDeviceConnected()

  fun isReadyForBleInteraction(): Boolean

  fun setNavigation()

  fun setTitle(title: String)

}