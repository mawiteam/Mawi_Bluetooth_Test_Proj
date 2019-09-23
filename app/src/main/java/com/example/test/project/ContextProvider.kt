package com.example.test.project

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
internal object ContextProvider {

  private var context: Context? = null

  @JvmStatic
  fun initialize(context: Context) {
    ContextProvider.context = context
  }

  val isInitialized: Boolean
    get() = context != null

  fun context(): Context {
    checkNotNull(context) { "Application context is null!" }

    return context!!
  }
}