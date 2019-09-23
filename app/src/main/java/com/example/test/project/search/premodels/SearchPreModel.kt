package com.example.test.project.search.premodels

import com.example.test.project.utils.base.PreModel

data class SearchPreModel(
    val macAddress: String,
    val name: String,
    val signalLevel: Int
) : PreModel(macAddress.hashCode().toLong())