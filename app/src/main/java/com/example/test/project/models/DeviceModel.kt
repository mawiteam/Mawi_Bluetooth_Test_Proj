package com.example.test.project.models

data class DeviceModel(
    val address: String,
    val name: String,
    var manufacturer: String,
    var modelNumber: String,
    var hwRevision: String,
    var fwRevision: String,
    var serialNumber: String
) {

  companion object {
    fun empty(address: String, name: String): DeviceModel {
      return DeviceModel(address, name, "", "", "", "", "")
    }

    val EMPTY: DeviceModel
      get() = DeviceModel("", "", "", "", "", "", "")
  }

}