package com.example.test.project.di

import com.example.test.project.di.home.HomeComponent
import band.mawi.android.sample.di.home.HomeModule
import com.example.test.project.di.search.SearchComponent
import com.example.test.project.di.search.SearchModule

object AppInjector {

  val appComponent: AppComponent by lazy {
    DaggerAppComponent.builder()
        .appModule(AppModule)
        .build()
  }

  var searchComponent: SearchComponent? = null
    get() {
      if (field != null) return field

      field = appComponent.addComponent(SearchModule())
      return field
    }

  var homeComponent: HomeComponent? = null
    get() {
      if (field != null) return field

      field = appComponent.addComponent(HomeModule())
      return field
    }
}