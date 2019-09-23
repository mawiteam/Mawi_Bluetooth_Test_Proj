package com.example.test.project.search

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.test.project.search.DiffUtilsCallback
import com.example.test.project.search.holders.SearchViewHolder
import com.example.test.project.search.premodels.SearchPreModelHandler

class SearchAdapter : ListAdapter<SearchPreModelHandler, SearchViewHolder>(DiffUtilsCallback()) {

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
    return SearchViewHolder.inflate(parent)
  }

  override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}