package com.example.test.project.screens.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.test.project.screens.MainActivity

abstract class BaseFragment : Fragment() {

  protected abstract var layoutId: Int

  protected fun currentActivity() = activity as? MainActivity

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
      inflater.inflate(layoutId, container, false)
}