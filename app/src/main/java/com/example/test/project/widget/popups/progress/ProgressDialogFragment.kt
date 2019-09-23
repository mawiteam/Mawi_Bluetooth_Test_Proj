package com.example.test.project.widget.popups.progress

import android.os.Bundle
import android.view.View
import com.example.test.project.widget.popups.BaseAlertDialog
import com.example.test.project.R

class ProgressDialogFragment : BaseAlertDialog() {

  companion object {

    private const val IS_CANCELABLE = "is_cancelable"
    private const val TEXT_RESOURCE = "TEXT_RESOURCE"

    fun instance(text: String, isCancelable: Boolean): ProgressDialogFragment {
      val dialog = ProgressDialogFragment()

      val bundle = Bundle()
      bundle.putBoolean(IS_CANCELABLE, isCancelable)
      bundle.putString(TEXT_RESOURCE, text)

      dialog.arguments = bundle

      return dialog
    }
  }

  override fun onViewBound(view: View) {
    val progress = view.findViewById<ProgressDialogView>(R.id.progress)

    arguments?.run {
      progress.setMessage(getString(TEXT_RESOURCE))
    }
  }

  override val layoutId: Int
    get() = R.layout.dialog_progress

  override fun handleCancel(): Boolean {
    return arguments?.getBoolean(IS_CANCELABLE) ?: false
  }

  override fun handleBack() {

  }
}