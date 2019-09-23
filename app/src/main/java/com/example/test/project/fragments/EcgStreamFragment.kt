package com.example.test.project.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import band.mawi.android.bluetooth.MawiBluetoothClient
import band.mawi.android.bluetooth.models.OperationResult
import band.mawi.android.bluetooth.models.ScreeningEvent
import com.example.test.project.di.AppInjector
import com.example.test.project.utils.id
import com.example.test.project.widget.EcgChartHelper
import com.example.test.project.R
import com.example.test.project.utils.operationResult
import com.github.mikephil.charting.charts.LineChart
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EcgStreamFragment : BaseFragment() {

  companion object {
    fun fragment(): EcgStreamFragment {
      return EcgStreamFragment()
    }
  }

  init {
    AppInjector.homeComponent?.inject(this)
  }

  @Inject
  lateinit var bluetoothClient: MawiBluetoothClient

  private val lineChart: LineChart by id(R.id.lc_ecg)
  private val tvHeartRate: TextView by id(R.id.tv_heart_rate)
  private val tvSamplingStatus: TextView by id(R.id.tv_sampling_status)

  private lateinit var chartHelper: EcgChartHelper

  override val layoutId: Int
    get() = R.layout.fragment_ecg_stream

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootDispatcher.setTitle("ECG Stream")
    rootDispatcher.setNavigation()
    chartHelper = EcgChartHelper(lineChart)

  }

  override fun onResume() {
    super.onResume()

    bluetoothClient.screeningController().data()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .operationResult { data ->
          if (data.rawSamples.isEmpty()) return@operationResult
          chartHelper.addEntry(data.rawSamples.map { it.toDouble() }.toDoubleArray())
        }
        .disposeOnPause()

    bluetoothClient.screeningController().events()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { event ->
          when (event) {

            is ScreeningEvent.PackageLoss -> {
            }

            is ScreeningEvent.Session -> {
              tvSamplingStatus.text = "Sampling: ${event.sampling}"
            }

          }
        }.disposeOnPause()

    bluetoothClient.screeningController().heartRate()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .operationResult { data ->
          tvHeartRate.text = "Heart Rate: ${data.value}"
        }
        .disposeOnPause()
  }

  override fun handleBack(): Boolean {
    return true
  }
}