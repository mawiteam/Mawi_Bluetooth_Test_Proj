package com.example.test.project.search.holders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.test.project.search.premodels.SearchPreModel
import com.example.test.project.search.premodels.SearchPreModelAction
import com.example.test.project.search.premodels.SearchPreModelHandler
import com.example.test.project.utils.base.BaseViewHolder
import com.example.test.project.utils.id
import com.example.test.project.R

class SearchViewHolder(
    view: View
) : BaseViewHolder<SearchPreModelAction, SearchPreModel, SearchPreModelHandler>(view) {

  private val tvName: TextView by id(R.id.tv_name)
  private val tvAddress: TextView by id(R.id.tv_address)
  private val ivRssi: ImageView by id(R.id.iv_rssi)

  companion object {
    fun inflate(parent: ViewGroup): SearchViewHolder {
      return SearchViewHolder(
          LayoutInflater.from(parent.context)
              .inflate(R.layout.item_view_search_result, parent, false)
      )
    }
  }

  init {
    itemView.setOnClickListener {
      action { SearchPreModelAction.DeviceSelected(it) }
    }
  }

  override fun bind(preModel: SearchPreModel) {
    tvName.text = preModel.name
    tvAddress.text = preModel.macAddress
    ivRssi.setImageLevel(preModel.signalLevel)
  }
}