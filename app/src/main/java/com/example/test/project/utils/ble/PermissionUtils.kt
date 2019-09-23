package com.example.test.project.utils.ble

import com.example.test.project.ContextProvider
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object PermissionUtils {

  private var disposable: Disposable? = null

  fun grantedForBle(): Boolean {
    val rxPermission = RealRxPermission.getInstance(ContextProvider.context())
    return rxPermission.isGranted(Manifest.ACCESS_COARSE_LOCATION) &&
        rxPermission.isGranted(Manifest.ACCESS_FINE_LOCATION)
  }

  fun tryToGrantForBle(func: () -> Unit) {
    val rxPermission = RealRxPermission.getInstance(ContextProvider.context())

    val permissions = hashMapOf(
        Manifest.ACCESS_COARSE_LOCATION to false,
        Manifest.ACCESS_FINE_LOCATION to false
    )

    disposable = rxPermission.requestEach(*permissions.keys.toTypedArray())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { permission ->
          permissions[permission.name()] = permission.state() == Permission.State.GRANTED
          if (permissions.all { it.value }) {
            func.invoke()
            disposable?.dispose()
            disposable = null
          }
        }
  }
}