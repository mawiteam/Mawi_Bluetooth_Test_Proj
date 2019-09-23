package com.example.test.project.utils.base

interface PreModelHandler<PreModel, Action> {

  val preModel: PreModel

  fun onAction(action: Action)
}