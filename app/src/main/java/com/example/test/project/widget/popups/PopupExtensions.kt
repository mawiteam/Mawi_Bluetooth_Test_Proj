package com.example.test.project.widget.popups

import androidx.appcompat.app.AppCompatActivity
import com.example.test.project.fragments.BaseFragment
import com.example.test.project.widget.popups.BaseAlertDialog
import com.example.test.project.widget.popups.progress.ProgressDialogFragment

fun BaseFragment.progressDialog(
    text: String,
    isCancelable: Boolean = true
): ProgressDialogFragment {
  return ProgressDialogFragment.instance(
      text, isCancelable
  )
}

fun AppCompatActivity.progressDialog(
    text: String,
    isCancelable: Boolean = true
): ProgressDialogFragment {
  return ProgressDialogFragment.instance(
      text, isCancelable
  )
}

fun BaseAlertDialog.dismissIfVisible() {
  this.dismiss()
}