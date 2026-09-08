package com.example.habitflow.data.repository


import com.example.habitflow.data.local.room.datasource.LocalHabitDataSource
import com.example.habitflow.data.local.room.mapper.toDomain
import com.example.habitflow.data.local.room.mapper.toEntity
import com.example.habitflow.data.model.toDomain
import com.example.habitflow.data.remote.FirestoreHabitDataSource
import com.example.habitflow.domain.model.Habit
import javax.inject.Inject
import com.example.habitflow.domain.repository.HabitRepository


class HabitRepositoryImpl @Inject constructor(
    private val localDataSource: LocalHabitDataSource,
    private val remoteDataSource: FirestoreHabitDataSource
) : HabitRepository {

    override suspend fun createHabit(
        habit: Habit
    ): Result<Unit> {

        return try {

            localDataSource.insertHabit(
                habit.toEntity()
            )

            remoteDataSource
                .createHabit(habit)
                .getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHabit(
        userId: String,
        habitId: String
    ): Result<Habit> {

        return try {

            val habit = localDataSource
                .getHabit(habitId)
                ?: return Result.failure(
                    NoSuchElementException("Habit not found")
                )

            Result.success(habit.toDomain())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHabits(
        userId: String
    ): Result<List<Habit>> {

        return try {

            val habits = localDataSource
                .getActiveHabits(userId)
                .map { it.toDomain() }

            Result.success(habits)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHabit(
        habit: Habit
    ): Result<Unit> {

        return try {

            localDataSource.updateHabit(
                habit.toEntity()
            )

            remoteDataSource
                .updateHabit(habit)
                .getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHabit(
        userId: String,
        habitId: String
    ): Result<Unit> {

        return try {

            localDataSource.deleteHabit(habitId)

            remoteDataSource
                .deleteHabit(userId, habitId)
                .getOrThrow()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}