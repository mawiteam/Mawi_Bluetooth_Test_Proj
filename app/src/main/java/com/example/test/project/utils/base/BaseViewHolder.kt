package com.example.test.project.utils.base

import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
abstract class BaseViewHolder<Action, PreModel, Handler: PreModelHandler<*, Action>>(
    view: View
) : RecyclerView.ViewHolder(view) {

  protected val disposables: CompositeDisposable = CompositeDisposable()
  protected var handler: Handler? = null

  fun bind(handler: Handler) {
    this.handler = handler
    bind(handler.preModel as PreModel)
  }

  protected open fun bind(preModel: PreModel) {}

  fun action(block: (preModel: PreModel) -> Action) {
    handler?.preModel?.let { handler?.onAction(block(it as PreModel)) }
  }

  @CallSuper
  open fun clear() {
    disposables.clear()
  }

  open fun attach() {}

  @CallSuper
  open fun detach() {
    disposables.clear()
  }
}