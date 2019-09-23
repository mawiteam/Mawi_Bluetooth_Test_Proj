package com.example.test.project.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import band.mawi.android.bluetooth.MawiBluetoothClient
import band.mawi.android.bluetooth.models.FitnessEvent
import band.mawi.android.bluetooth.models.OperationResult
import com.example.test.project.R
import com.example.test.project.di.AppInjector
import com.example.test.project.utils.operationResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ReadFitnessHistoryFragment : BaseFragment() {

  companion object {

    private val TAG = ReadFitnessHistoryFragment::class.java.simpleName

    fun fragment(): ReadFitnessHistoryFragment {
      return ReadFitnessHistoryFragment()
    }
  }

  init {
    AppInjector.homeComponent?.inject(this)
  }

  @Inject
  lateinit var bluetoothClient: MawiBluetoothClient

  override val layoutId: Int
    get() = R.layout.fragment_read_fitness_history

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootDispatcher.setTitle("Read Fitness History")
    rootDispatcher.setNavigation()

  }

  override fun onResume() {
    super.onResume()

    bluetoothClient.fitnessController().data()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .operationResult { data ->
          Log.i(TAG, data.toString())
        }
        .disposeOnPause()

    bluetoothClient.fitnessController().events()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { event ->
          when (event) {
            is FitnessEvent.Started -> {
              Log.i(TAG, "Batching is started. Chunks count = ${event.chunksCount}")
            }
            FitnessEvent.Stoped -> {
              Log.i(TAG, "Batching is stopped.")

            }
          }

        }.disposeOnPause()
  }
}