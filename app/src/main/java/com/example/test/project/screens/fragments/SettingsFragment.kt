package com.example.test.project.screens.fragments

import android.os.Bundle
import android.view.View
import band.mawi.android.bluetooth.model.BandLocation
import band.mawi.android.bluetooth.model.Battery
import band.mawi.android.bluetooth.model.Device
import com.example.test.project.R
import com.example.test.project.listeners.SettingsActionListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_settings.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener

class SettingsFragment : BaseFragment(), SettingsActionListener {

  override var layoutId = R
      .layout.fragment_settings

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    currentActivity()?.onSettingsActionListener = this

    settings_band_location_spinner.onItemSelectedListener {
      onItemSelected { _, _, i, _ ->
        currentActivity()?.setBandLocation(i)
      }
    }

    settings_disconnect_button.onClick { currentActivity()?.disconnect() }
  }

  override fun onBatteryLevelChange(battery: Battery) {
    settings_battery_level.text = resources.getString(R.string.battery_level, battery.level)
  }

  override fun onBandLocationChange(location: BandLocation) {
    settings_band_location_spinner.setSelection(location.ordinal)
  }

  override fun onDeviceInformationChange(device: Device) {
    settings_device_information.text = formatString(Gson().toJson(device))
  }

  override fun onDestroyView() {
    super.onDestroyView()
    currentActivity()?.onSettingsActionListener = null
  }

  private fun formatString(text: String): String {
    val builder = StringBuilder()
    var indentString = ""
    text.forEach {
      when (it) {
        '{' -> {}
        '[' -> {
          builder.append("\n$indentString$it\n")
          indentString += "\t"
          builder.append(indentString)
        }

        '}' -> {}
        ']' -> {
          indentString = indentString.replaceFirst("\t", "")
          builder.append("\n$indentString$it")
        }
        ',' -> builder.append("$it\n$indentString")
        else -> builder.append(it)
      }
    }
    return builder.toString()
  }

}