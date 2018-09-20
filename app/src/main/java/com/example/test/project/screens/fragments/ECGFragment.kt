package com.example.test.project.screens.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.test.project.R
import com.example.test.project.listeners.OnECGActionListener
import com.example.test.project.toMainThread
import com.example.test.project.widget.ECGChartHelper
import com.google.common.collect.Lists
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_ecg.*
import org.jetbrains.anko.getStackTraceString
import java.util.concurrent.TimeUnit

class ECGFragment : BaseFragment(), OnECGActionListener {

  private val TAG = this@ECGFragment.javaClass.simpleName

  override var layoutId = R.layout.fragment_ecg

  private lateinit var chartHelper: ECGChartHelper

  private val PARTITION_SIZE = 8

  private var ecgSubject = PublishSubject.create<List<List<Double>>>()

  @SuppressLint("CheckResult")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    currentActivity()?.onECGActionListener = this

    chartHelper = ECGChartHelper(ecg_line_chart)

    ecgSubject.flatMap { Observable.fromIterable(it) }
        .concatMap { Observable.just(it).delay(14L, TimeUnit.MILLISECONDS) }
        .toMainThread()
        .subscribe({ chartHelper.addEntry(it.toDoubleArray()) }, { Log.e(TAG, it.getStackTraceString()) })
  }

  override fun onDataLoaded(data: DoubleArray) {
    ecgSubject.onNext(Lists.partition(data.toList(), PARTITION_SIZE))
  }

  override fun onSessionChanged(isAttached: Boolean) {
    ecg_packets_info.text = resources.getString(R.string.is_attached, isAttached)
  }

  override fun onHeartRateChanged(heartRate: Int) {
    ecg_heart_rate.text = resources.getString(R.string.heart_rate, heartRate)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    currentActivity()?.onECGActionListener = null
  }
}