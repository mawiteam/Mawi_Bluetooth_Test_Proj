package com.example.test.project.utils

import android.util.Log
import band.mawi.android.bluetooth.models.OperationResult
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposeBy(disposableBag: CompositeDisposable) {
  disposableBag.add(this)
}

fun <Type> ObservableEmitter<Type>.tryDo(
    code: ObservableEmitter<Type>.() -> Unit
) {
  if (!isDisposed) code()
}

fun <Type> SingleEmitter<Type>.tryDo(
    code: SingleEmitter<Type>.() -> Unit
) {
  if (!isDisposed) code()
}

fun <Type> FlowableEmitter<Type>.tryDo(
    code: FlowableEmitter<Type>.() -> Unit
) {
  if (!isCancelled) code()
}

fun CompletableEmitter.tryDo(
    code: CompletableEmitter.() -> Unit
) {
  if (!isDisposed) code()
}

fun <T> Observable<OperationResult<T>>.operationResult(success: (data: T) -> Unit): Disposable {
  return operationResult(success) { message, status ->
    Log.e(this::class.java.simpleName, message)
  }
}

fun <T> Observable<OperationResult<T>>.operationResult(
    success: (data: T) -> Unit = {},
    failure: (message: String, status: Int) -> Unit = { _, _ -> }
): Disposable {
  return subscribe { handleResult(it, success, failure) }
}

