package com.example.test.project

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonProvider {

  private val gson: Gson by lazy {
    val builder = GsonBuilder()
    builder.serializeSpecialFloatingPointValues()
    builder.setLenient()
    builder.create()
  }

  fun gson(): Gson {
    return gson
  }

}