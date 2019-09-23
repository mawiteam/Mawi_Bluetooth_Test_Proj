package com.example.test.project.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.test.project.activities.RootDispatcher
import com.example.test.project.utils.ViewBinder
import com.example.test.project.utils.disposeBy
import com.example.test.project.widget.popups.dismissIfVisible
import com.example.test.project.widget.popups.progress.ProgressDialogFragment
import com.example.test.project.widget.popups.progressDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.extensions.LayoutContainer

abstract class BaseFragment : Fragment(), LayoutContainer {

  override var containerView: View? = null

  protected abstract val layoutId: Int

  protected val disposableBag: CompositeDisposable = CompositeDisposable()

  private var progressDialog: ProgressDialogFragment? = null

  protected val rootDispatcher: RootDispatcher
    get() = (activity as RootDispatcher)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    ViewBinder.reset(this)
    containerView = inflater.inflate(layoutId, container, false)
    return containerView!!
  }

  protected fun Disposable.disposeOnPause() {
    disposeBy(disposableBag)
  }

  override fun onPause() {
    super.onPause()
    disposableBag.clear()
  }

  override fun onStop() {
    super.onStop()
    ViewBinder.reset(this)
  }

  open fun handleBack(): Boolean {
    return true
  }

  protected fun appeardProgress(
      text: String = "Loading",
      isCancelable: Boolean = true
  ) {

    progressDialog?.dismissIfVisible()

    progressDialog = progressDialog(text, isCancelable)
    progressDialog?.show(fragmentManager!!, "progress_dialog")
  }

  fun disappearProgressDialog() {
    progressDialog?.dismissIfVisible()
  }


  fun replace(fragment: BaseFragment) {
  }

  fun push(to: BaseFragment) {
    push(fragmentManager, to)
  }

  fun pushParent(to: BaseFragment) {
    push(parentFragment?.fragmentManager, to)
  }

  private fun push(fragmentManager: FragmentManager?, to: BaseFragment) {
    val tag = to.javaClass.simpleName

    if (fragmentManager?.findFragmentByTag(tag) != null || containerView == null)
      return

    fragmentManager?.beginTransaction()
        ?.replace(containerView!!.id, to)
        ?.addToBackStack(tag)
        ?.commit()
  }
}
