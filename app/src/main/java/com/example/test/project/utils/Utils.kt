package com.example.test.project.utils

import android.os.Handler
import android.os.Looper
import band.mawi.android.bluetooth.models.OperationResult

inline fun runOnUiThread(delay: Long, crossinline run: () -> Unit = {}) {
  Handler(Looper.getMainLooper()).postDelayed({ run.invoke() }, delay)
}

inline fun runOnUiThread(crossinline run: () -> Unit = {}) {
  Handler(Looper.getMainLooper()).post { run.invoke() }
}

inline fun <T> handleResult(
    result: OperationResult<T>,
    success: (T) -> Unit = {},
    failure: (message: String, status: Int) -> Unit = { _, _ -> }
) {
  when (result) {
    is OperationResult.Success -> {
      val data = result.data ?: return
      success.invoke(data)
    }
    is OperationResult.Failure -> {
      failure.invoke(result.message, result.status)
    }
  }
}