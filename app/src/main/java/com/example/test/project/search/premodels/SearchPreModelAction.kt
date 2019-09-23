package com.example.test.project.search.premodels

sealed class SearchPreModelAction {
  data class DeviceSelected(val devicePreModel: SearchPreModel) : SearchPreModelAction()
}