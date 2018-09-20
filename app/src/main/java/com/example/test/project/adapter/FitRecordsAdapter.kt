package com.example.test.project.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import band.mawi.android.bluetooth.model.fit.FitRecord
import com.example.test.project.widget.RecordItemView

class FitRecordsAdapter(private val activity: Activity?): RecyclerView.Adapter<FitRecordsAdapter.RecordItemViewHolder>() {

  private val list = arrayListOf<FitRecord>()

  fun addAll(items: List<FitRecord>) {
    list.clear()
    list.addAll(items)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecordItemViewHolder(RecordItemView(activity))

  override fun getItemCount() = list.size

  override fun onBindViewHolder(holder: RecordItemViewHolder, position: Int) =
      (holder.itemView as RecordItemView).setData(list[position])

  inner class RecordItemViewHolder(itemView: RecordItemView): RecyclerView.ViewHolder(itemView)
}