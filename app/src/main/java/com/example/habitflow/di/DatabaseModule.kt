package com.example.habitflow.di

import android.content.Context
import androidx.room.Room
import com.example.habitflow.data.local.room.HabitDatabase
import com.example.habitflow.data.local.room.dao.CompletionDao
import com.example.habitflow.data.local.room.dao.HabitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {

        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        ).build()
    }

    @Provides
    fun provideHabitDao(
        database: HabitDatabase
    ): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideCompletionDao(
        database: HabitDatabase
    ): CompletionDao {
        return database.completionDao()
    }
}