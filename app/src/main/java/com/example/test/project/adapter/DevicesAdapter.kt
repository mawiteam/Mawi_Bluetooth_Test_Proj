package com.example.test.project.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.example.test.project.widget.DeviceItemView
import com.polidea.rxandroidble2.scan.ScanResult
import org.jetbrains.anko.sdk25.coroutines.onClick

class DevicesAdapter(val activity: Activity?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val list = arrayListOf<ScanResult>()

  fun add(item: ScanResult) {
    list.forEachIndexed { index, scanResult ->
      if (scanResult.bleDevice.macAddress == item.bleDevice.macAddress) {
        list[index] = item
        notifyItemChanged(index)
        return
      }
    }

    list.add(item)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DeviceItemViewHolder(DeviceItemView(activity))

  override fun getItemCount() = list.size

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
      (holder.itemView as DeviceItemView).setData(list[position])

  inner class DeviceItemViewHolder(itemView: DeviceItemView) : RecyclerView.ViewHolder(itemView) {
    init {
      itemView.onClick {
        if (adapterPosition != -1)
          (activity as? OnDeviceClickListener)?.onDeviceClick(list[adapterPosition])
      }
    }
  }

  interface OnDeviceClickListener {
    fun onDeviceClick(scanResult: ScanResult)
  }
}