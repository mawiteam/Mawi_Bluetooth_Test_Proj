package com.example.test.project.widget.popups

import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.test.project.R
import kotlinx.android.extensions.LayoutContainer

abstract class BaseAlertDialog : DialogFragment(), LayoutContainer {

  override var containerView: View? = null

  protected abstract fun onViewBound(view: View)

  protected abstract val layoutId: Int

  open fun handleCancel(): Boolean {
    return true
  }

  open fun handleBack() {

  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    containerView = View.inflate(activity, layoutId, null)
    onViewBound(containerView!!)

    val builder = AlertDialog.Builder(activity!!, R.style.TransparentDialog)
    builder.setView(containerView)

    val dialog = builder.create()
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCanceledOnTouchOutside(handleCancel())

    dialog.setOnKeyListener { _, keyCode, _ ->

      if (keyCode == KeyEvent.KEYCODE_BACK)
        handleBack()

      true
    }

    setupWindow(dialog.window)


    return dialog
  }

  private fun setupWindow(windowSrc: Window?) {
    windowSrc?.let {
      val params = WindowManager.LayoutParams()
      params.copyFrom(it.attributes)
      params.dimAmount = 0.6f
      params.width = calculateMaxWidth()
      params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND

      it.attributes = params
    }
  }

  private fun calculateMaxWidth(): Int {
    return getWindowWidth()
  }

  private fun getWindowWidth(): Int {
    val display = ContextCompat.getSystemService(
        context!!,
        WindowManager::class.java
    )?.defaultDisplay!!

    val size = Point()

    display.getSize(size)

    return size.x
  }
}