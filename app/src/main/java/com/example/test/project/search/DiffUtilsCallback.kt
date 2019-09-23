package com.example.test.project.search

import androidx.recyclerview.widget.DiffUtil
import com.example.test.project.search.premodels.SearchPreModelHandler

class DiffUtilsCallback : DiffUtil.ItemCallback<SearchPreModelHandler>() {

  override fun areItemsTheSame(
      oldItem: SearchPreModelHandler,
      newItem: SearchPreModelHandler
  ): Boolean {
    return false
  }

  override fun areContentsTheSame(
      oldItem: SearchPreModelHandler,
      newItem: SearchPreModelHandler
  ): Boolean {
    return false
  }
}