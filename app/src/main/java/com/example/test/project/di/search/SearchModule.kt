package com.example.test.project.di.search

import band.mawi.android.bluetooth.MawiBluetoothClient
import com.example.test.project.viewmodels.SearchViewModel
import dagger.Module
import dagger.Provides

@Module
class SearchModule {

  @SearchScope
  @Provides
  fun provideSearchViewModel(
      client: MawiBluetoothClient
  ): SearchViewModel {
    return SearchViewModel(client)
  }
}