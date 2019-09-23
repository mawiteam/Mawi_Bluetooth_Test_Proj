package com.example.test.project.prefs

import android.content.SharedPreferences
import com.example.test.project.GsonProvider
import com.example.test.project.utils.NullableReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal sealed class PreferencesDelegate<Type>(
    protected val key: kotlin.String
) : NullableReadWriteProperty<SharedPreferences, Type> {

  class Int(key: kotlin.String) : PreferencesDelegate<kotlin.Int>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): kotlin.Int {
      return thisRef.getInt(key, 0)
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: kotlin.Int?
    ) {
      thisRef.edit().putInt(key, value ?: 0).apply()
    }
  }

  class String(key: kotlin.String) : PreferencesDelegate<kotlin.String>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): kotlin.String {
      return thisRef.getString(key, "") ?: ""
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: kotlin.String?
    ) {
      thisRef.edit().putString(key, value ?: "").apply()
    }
  }

  class Long(key: kotlin.String) : PreferencesDelegate<kotlin.Long>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): kotlin.Long {
      return thisRef.getLong(key, 0)
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: kotlin.Long?
    ) {
      thisRef.edit().putLong(key, value ?: 0L).apply()
    }
  }

  class Boolean(key: kotlin.String) : PreferencesDelegate<kotlin.Boolean>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): kotlin.Boolean {
      return thisRef.getBoolean(key, false)
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: kotlin.Boolean?
    ) {
      thisRef.edit().putBoolean(key, value ?: false).apply()
    }
  }

  class Float(key: kotlin.String) : PreferencesDelegate<kotlin.Float>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): kotlin.Float {
      return thisRef.getFloat(key, -1f)
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: kotlin.Float?
    ) {
      thisRef.edit().putFloat(key, value ?: -1f).apply()
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Enum<T : kotlin.Enum<*>>(
      key: kotlin.String,
      enumClass: KClass<T>
  ) : PreferencesDelegate<T>(key) {

    private val enumConstants = enumClass.java.enumConstants

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): T {
      val savedOrdinal = thisRef.getInt(key, 0)
      return enumConstants!!.first { it.ordinal == savedOrdinal }
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: T?
    ) {
      thisRef.edit().putInt(key, value?.ordinal ?: 0).apply()
    }
  }

  class Model<T : Any>(
      key: kotlin.String,
      private val modelClass: KClass<T>
  ) : PreferencesDelegate<T>(key) {

    override fun getValue(
        thisRef: SharedPreferences,
        property: KProperty<*>
    ): T? {

      val json = thisRef.getString(key, "")
      if (json.isNullOrEmpty()) return null

      val gson = GsonProvider.gson()

      return gson.fromJson<T>(json, modelClass.java)
    }

    override fun setValue(
        thisRef: SharedPreferences,
        property: KProperty<*>,
        value: T?
    ) {
      val gson = GsonProvider.gson()
      val json = gson.toJson(value)
      thisRef.edit().putString(key, json ?: "").apply()
    }
  }
}