package com.example.test.project.utils.ble

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import band.mawi.android.bluetooth.MawiBluetooth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

object BleReadiness {

  private val publisher: PublishSubject<Unit> = PublishSubject.create()

  fun isReady(activity: AppCompatActivity): Boolean {

    if (PermissionUtils.grantedForBle()
        && GpsProvider.isGpsEnabled()
        && MawiBluetooth.isBleEnabled
    ) {
      return true
    }

    PermissionUtils.tryToGrantForBle {
      if (!GpsProvider.isGpsEnabled()) {
        GpsProvider.tryToEnableGps(activity)
      } else {
        BleUtils.checkBle(activity, BleReadiness::ready)
      }
    }

    return false
  }

  private fun ready() {
    publisher.onNext(Unit)
  }

  fun listen(closure: () -> Unit): Disposable {
    return publisher.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          closure.invoke()
        }
  }

  fun checkResult(activity: AppCompatActivity, requestCode: Int, resultCode: Int) {
    when (requestCode) {
      BleUtils.ENABLE_BLE_RC -> when (resultCode) {
        Activity.RESULT_OK -> ready()
        Activity.RESULT_CANCELED -> BleUtils.checkBle(activity)
      }


      GpsProvider.ENABLE_GPS_RC -> {
        when (resultCode) {
          Activity.RESULT_CANCELED -> {
            if (GpsProvider.isGpsEnabled()) {
              BleUtils.checkBle(activity, BleReadiness::ready)
            }
          }
        }
      }
    }
  }
}