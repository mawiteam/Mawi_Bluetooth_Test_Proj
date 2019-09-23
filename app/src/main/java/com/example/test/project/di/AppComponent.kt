package com.example.test.project.di

import com.example.test.project.activities.RootActivity
import com.example.test.project.di.home.HomeComponent
import band.mawi.android.sample.di.home.HomeModule
import com.example.test.project.di.search.SearchComponent
import com.example.test.project.di.search.SearchModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent  {

  fun addComponent(searchModule: SearchModule): SearchComponent

  fun addComponent(homeModule: HomeModule): HomeComponent


  fun inject(rootActivity: RootActivity)
}