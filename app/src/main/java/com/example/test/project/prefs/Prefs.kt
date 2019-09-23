package com.example.test.project.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.test.project.ContextProvider
import com.example.test.project.models.DeviceModel
import com.example.test.project.utils.tryDo
import io.reactivex.Observable

internal object Prefs {

  private object Key {
    const val DEVICE_KEY = "device"
  }

  private const val PREFERENCES_NAME = "mawi-storage"

  private val prefs: SharedPreferences by lazy {
    ContextProvider.context().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
  }

  private var SharedPreferences.device: DeviceModel?
      by PreferencesDelegate.Model(Key.DEVICE_KEY, DeviceModel::class)

  var device: DeviceModel?
    get() = prefs.device
    set(value) {
      prefs.device = value
    }

  fun device(): Observable<DeviceModel> {
    return Observable.create { emitter ->
      if (device == null) {
        emitter.onComplete()
        return@create
      }

      emitter.onNext(device!!)

      val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key != PREFERENCES_NAME) return@OnSharedPreferenceChangeListener
        val device = sharedPreferences.device ?: return@OnSharedPreferenceChangeListener

        emitter.tryDo { onNext(device) }
      }

      prefs.registerOnSharedPreferenceChangeListener(listener)

      emitter.setCancellable {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
      }
    }
  }

}