package com.example.test.project.utils.ble

import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.test.project.ContextProvider

object GpsProvider {

  const val ENABLE_GPS_RC = 101

  private var dialog: AlertDialog? = null

  fun isGpsEnabled(): Boolean {
    val locationManager = ContextCompat.getSystemService(
        ContextProvider.context(),
        LocationManager::class.java
    ) ?: return false

    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
      return true

    return false
  }

  fun tryToEnableGps(activity: AppCompatActivity) {
    dialog = AlertDialog.Builder(activity)
        .setTitle("Enable GPS")
        .setMessage("Turn onn GPS for the application to work properly with the sensor.")
        .setCancelable(false)
        .setPositiveButton("Enable") { _, _ ->
          activity.startActivityForResult(
              Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
              ENABLE_GPS_RC
          )
        }.create()

    dialog?.show()
  }

  fun dismissIfVisible() {
    if (dialog != null && dialog?.isShowing == true) {
      dialog?.dismiss()
      dialog = null
    }
  }
}