package com.example.test.project.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.project.search.SearchAdapter
import com.example.test.project.di.AppInjector
import com.example.test.project.utils.ble.BleReadiness
import com.example.test.project.utils.id
import com.example.test.project.viewmodels.SearchViewModel
import com.example.test.project.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchFragment : BaseFragment() {

  companion object {
    fun fragment(): SearchFragment {
      return SearchFragment()
    }
  }

  init {
    AppInjector.searchComponent?.inject(this)
  }

  @Inject
  lateinit var searchViewModel: SearchViewModel

  private val contentAdapter = SearchAdapter()

  private val rvContentList: RecyclerView by id(R.id.rv_devices)

  override val layoutId: Int
    get() = R.layout.fragment_search

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    rootDispatcher.setTitle("Search Devices")
    rvContentList.run {
      layoutManager = LinearLayoutManager(view.context)
      itemAnimator = DefaultItemAnimator()
      adapter = contentAdapter
    }
  }

  override fun handleBack(): Boolean {
    return false
  }

  override fun onResume() {
    super.onResume()

    if (rootDispatcher.isReadyForBleInteraction()) {
      subscribeForData()
      return
    }

    BleReadiness.listen {
      subscribeForData()
    }.disposeOnPause()
  }

  private fun subscribeForData() {
    searchViewModel.devices()
        .delaySubscription(200L, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(contentAdapter::submitList)
        .disposeOnPause()

    searchViewModel.events()
        .delaySubscription(200L, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::onEvent)
        .disposeOnPause()
  }

  private fun onEvent(event: SearchViewModel.Event) {
    when (event) {

      is SearchViewModel.Event.Processing -> {
        appeardProgress(event.message)
      }

      SearchViewModel.Event.DeviceConnected -> {
        disappearProgressDialog()
        rootDispatcher.dispatchDeviceConnected()
      }

      is SearchViewModel.Event.Error -> {
        disappearProgressDialog()
        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
      }

    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    AppInjector.searchComponent = null
  }
}