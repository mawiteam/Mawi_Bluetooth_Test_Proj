package com.example.test.project.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import band.mawi.android.bluetooth.MawiBluetooth
import com.example.test.project.fragments.BaseFragment
import com.example.test.project.fragments.HomeFragment
import com.example.test.project.fragments.SearchFragment
import com.example.test.project.prefs.Prefs
import com.example.test.project.utils.ble.BleReadiness
import com.example.test.project.utils.ble.GpsProvider
import com.example.test.project.utils.disposeBy
import com.example.test.project.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RootActivity : AppCompatActivity(), RootDispatcher {

  private val disposableBag: CompositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (!MawiBluetooth.isBleSupported) {
      Toast.makeText(
          this,
          "This device does not support Bluetooth Low Energy.",
          Toast.LENGTH_SHORT
      ).show()

      finish()

      return
    }

    setContentView(R.layout.activity_root)

    dispatchDeviceConnected()

  }

  override fun onStop() {
    super.onStop()
    disposableBag.clear()
    GpsProvider.dismissIfVisible()
  }

  private fun Disposable.disposeOnStop() {
    disposeBy(disposableBag)
  }

  override fun setNavigation() {
    supportActionBar?.let {
      it.setDefaultDisplayHomeAsUpEnabled(true)
      it.setDisplayHomeAsUpEnabled(true)

    }
  }

  override fun setTitle(title: String) {
    supportActionBar?.title = title
  }

  override fun isReadyForBleInteraction(): Boolean {
    return BleReadiness.isReady(this)
  }

  override fun dispatchDeviceConnected() {
    if (Prefs.device == null) {
      setRootFragment(SearchFragment.fragment())
    } else {
      setRootFragment(HomeFragment.fragment())
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> onBackPressed()
    }

    return true
  }

  override fun onBackPressed() {
//    val fragment = supportFragmentManager
//        .findFragmentByTag(HomeFragment::class.java.simpleName) as? BaseFragment
//
//    if (fragment == null || !fragment.handleBack())
      super.onBackPressed()
  }

  private fun setRootFragment(fragment: BaseFragment) {
    supportFragmentManager.beginTransaction()
        .replace(
            R.id.root_container,
            fragment,
            fragment::class.java.simpleName
        )
        .commit()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    BleReadiness.checkResult(this, requestCode, resultCode)
  }

}