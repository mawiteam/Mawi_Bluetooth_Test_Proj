package com.example.test.project.di.home

import band.mawi.android.sample.di.home.HomeModule
import band.mawi.android.sample.di.home.HomeScope
import com.example.test.project.fragments.EcgStreamFragment
import com.example.test.project.fragments.HomeFragment
import com.example.test.project.fragments.ReadFitnessHistoryFragment
import dagger.Subcomponent

@HomeScope
@Subcomponent(modules = [HomeModule::class])
interface HomeComponent {

  fun inject(homeFragment: HomeFragment)

  fun inject(ecgStreamFragment: EcgStreamFragment)

  fun inject(readFitnessHistoryFragment: ReadFitnessHistoryFragment)

}