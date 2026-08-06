package com.jenil.weather.di

import com.jenil.weather.data.repository.WindRepositoryImpl
import com.jenil.weather.domain.repository.WindRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WindRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWindRepository(
        impl: WindRepositoryImpl
    ): WindRepository
}