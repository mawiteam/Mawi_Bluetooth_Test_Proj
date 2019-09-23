package com.example.test.project.di.search

import com.example.test.project.fragments.SearchFragment
import dagger.Subcomponent


@SearchScope
@Subcomponent(modules = [SearchModule::class])
interface SearchComponent {

  fun inject (searchFragment: SearchFragment)

}