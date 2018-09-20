@file:Suppress("DEPRECATION")

package com.example.test.project.screens

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import band.mawi.android.bluetooth.source.MawiBluetoothClient
import com.example.test.project.SampleApplication
import com.example.test.project.batchRequestPermissions
import com.example.test.project.isPermissionGranted
import com.example.test.project.requestPermission
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.yesButton

abstract class BaseActivity : AppCompatActivity() {

  protected abstract val TAG: String

  protected abstract var layoutId: Int

  private var dialog: ProgressDialog? = null

  private val ENABLE_BLUETOOTH_REQUEST = 1
  private val ENABLE_LOCATION_SERVICES_REQUEST = 2
  private val ENABLE_LOCATION_PERMISSIONS_REQUEST = 3

  protected lateinit var bluetoothClient: MawiBluetoothClient

  protected open var fragmentContainer: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layoutId)

    bluetoothClient = SampleApplication.client
  }

  protected fun Toolbar.setupToolbar(title: String) {
    setSupportActionBar(this)
    supportActionBar?.let {
      it.title = title
    }
  }

  private fun checkBluetooth(func: () -> Unit) {
    val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    bluetoothManager?.let {
      when {
        it.adapter.isEnabled -> func()
        else -> enableBluetooth()
      }
    }
  }

  protected fun checkPermissions() {
    if (!isPermissionGranted(ACCESS_FINE_LOCATION) && !isPermissionGranted(ACCESS_COARSE_LOCATION)) {
      batchRequestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), ENABLE_LOCATION_PERMISSIONS_REQUEST)
    } else {
      checkLocationServices { checkBluetooth { onBluetoothEnabled() } }
    }
  }

  private fun enableBluetooth() {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(intent, ENABLE_BLUETOOTH_REQUEST)
  }

  private fun checkLocationServices(func: () -> Unit) {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    when {
      locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> func()
      else -> alert("For search devices need to turn on GPS tracking", "Enable GPS?") {
        yesButton { startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), ENABLE_LOCATION_SERVICES_REQUEST) }
      }.show()
    }
  }

  protected fun replaceFragment(fragment: Fragment, func: () -> Unit) {
    if (fragmentContainer != -1) {
      supportFragmentManager.beginTransaction()
          .replace(fragmentContainer, fragment)
          .commitAllowingStateLoss()
      func()
    }
  }

  open fun onBluetoothEnabled() {
    Log.d(TAG, "onBluetoothEnabled()")
  }

  open fun onLocationServicesEnabled() {
    Log.d(TAG, "onLocationServicesEnabled()")
    checkBluetooth { onBluetoothEnabled() }
  }

  protected fun showDialog() {
    dialog = indeterminateProgressDialog("Loading")
    dialog?.setCanceledOnTouchOutside(false)
  }

  protected fun hideDialog() {
    dialog?.dismiss()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (resultCode) {
      Activity.RESULT_OK -> {
        if (requestCode == ENABLE_BLUETOOTH_REQUEST)
          onBluetoothEnabled()
      }

      Activity.RESULT_CANCELED -> {
        if (requestCode == ENABLE_LOCATION_SERVICES_REQUEST)
          checkLocationServices { onLocationServicesEnabled() }
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == ENABLE_LOCATION_PERMISSIONS_REQUEST) {
      if (grantResults.contains(PERMISSION_GRANTED)) {
        checkLocationServices { checkBluetooth { onBluetoothEnabled() } }
      }
    }
  }
}