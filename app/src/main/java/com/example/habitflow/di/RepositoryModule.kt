package com.example.habitflow.di

import com.example.habitflow.data.repository.AuthRepositoryImpl
import com.example.habitflow.data.repository.CompletionRepositoryImpl
import com.example.habitflow.data.repository.HabitRepositoryImpl
import com.example.habitflow.data.repository.UserRepositoryImpl
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: AuthRepositoryImpl
    ): AuthRepository



    @Binds
    @Singleton
    abstract fun bindUserRepository(
        implementation: UserRepositoryImpl
    ): UserRepository


    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        implementation: HabitRepositoryImpl
    ): HabitRepository


    @Binds
    @Singleton
    abstract fun bindCompletionRepository(
        implementation: CompletionRepositoryImpl
    ): CompletionRepository



}