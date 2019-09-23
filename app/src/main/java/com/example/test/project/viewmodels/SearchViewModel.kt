package com.example.test.project.viewmodels

import band.mawi.android.bluetooth.MawiBluetooth
import band.mawi.android.bluetooth.MawiBluetoothClient
import band.mawi.android.bluetooth.models.DeviceState.DEVICE_READY_FOR_USE
import band.mawi.android.bluetooth.models.OperationResult
import band.mawi.android.bluetooth.models.scanner.SearchErrorType.*
import band.mawi.android.bluetooth.models.scanner.SearchResult
import com.example.test.project.search.premodels.SearchPreModel
import com.example.test.project.search.premodels.SearchPreModelAction
import com.example.test.project.search.premodels.SearchPreModelHandler
import com.example.test.project.models.DeviceModel
import com.example.test.project.prefs.Prefs
import com.example.test.project.utils.disposeBy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class SearchViewModel(private val client: MawiBluetoothClient) {

  private val disposableBag: CompositeDisposable = CompositeDisposable()

  private val eventsStream: PublishSubject<Event> =
      PublishSubject.create()

  private val dataStream: PublishSubject<List<SearchPreModelHandler>> =
      PublishSubject.create()

  sealed class Event {
    data class Processing(val message: String) : Event()
    object DeviceConnected : Event()
    data class Error(val message: String) : Event()
  }

  inner class Model(private val model: SearchPreModel) : SearchPreModelHandler {

    override val preModel: SearchPreModel
      get() = model

    override fun onAction(action: SearchPreModelAction) {
      when (action) {
        is SearchPreModelAction.DeviceSelected -> {
          connectDevice(action.devicePreModel)
        }
      }
    }
  }

  fun events(): Observable<Event> {
    return eventsStream
  }

  fun devices(): Observable<List<SearchPreModelHandler>> {
    if (!MawiBluetooth.isBleEnabled)
      return Observable.error(Throwable("Bluetooth does not enabled!"))

    return dataStream.doOnSubscribe {
      if (disposableBag.size() != 0) return@doOnSubscribe
      startSearchDevices()
    }.doOnDispose(disposableBag::clear)
  }

  private fun connectDevice(preModel: SearchPreModel) {
    disposableBag.clear()

    Prefs.device = DeviceModel.empty(preModel.macAddress, preModel.name)

    client.connect(preModel.macAddress)

    eventsStream.onNext(Event.Processing("Pairing"))

    client.deviceInformation()
        .subscribe { result ->
          when (result) {
            is OperationResult.Success -> {
              val deviceInformation = result.data ?: return@subscribe
              Prefs.device = Prefs.device?.copy(
                  manufacturer = deviceInformation.manufacturer,
                  modelNumber = deviceInformation.modelNumber,
                  hwRevision = deviceInformation.hwRevision,
                  fwRevision = deviceInformation.fwRevision,
                  serialNumber = deviceInformation.serialNumber
              )

            }
            is OperationResult.Failure -> {
              // do nothing
            }
          }
        }
        .disposeBy(disposableBag)

    client.connectionState()
        .subscribe {
          when (it) {
            DEVICE_READY_FOR_USE -> {
              eventsStream.onNext(Event.DeviceConnected)
              disposableBag.clear()
            }
          }
        }.disposeBy(disposableBag)
  }

  private fun startSearchDevices() {
    client.searchDevices().subscribe(::onResult)
        .disposeBy(disposableBag)
  }

  private fun onResult(searchResult: SearchResult) {
    when (searchResult) {

      is SearchResult.Batch ->
        dataStream.onNext(
            searchResult.devices.map {
              Model(SearchPreModel(it.address, it.name, it.rssiPercent))
            }
        )

      is SearchResult.Device ->
        dataStream.onNext(
            listOf(
                Model(SearchPreModel(
                    searchResult.address,
                    searchResult.name,
                    searchResult.rssiPercent
                ))
            )
        )

      is SearchResult.Error -> {
        when (searchResult.type) {
          ALREADY_STARTED ->
            eventsStream.onNext(Event.Error("Scan has already started."))

          OUT_OF_HARDWARE_RESOURCES ->
            eventsStream.onNext(Event.Error("Scan cannot start due to limited hardware resources."))

          FEATURE_UNSUPPORTED ->
            eventsStream.onNext(Event.Error("Scan with specified parameters is not support."))

          else ->
            eventsStream.onNext(Event.Error("Scan has failed due to an internal error.\n\nTry to enable and disable the airplane mode."))
        }
      }

    }
  }

}