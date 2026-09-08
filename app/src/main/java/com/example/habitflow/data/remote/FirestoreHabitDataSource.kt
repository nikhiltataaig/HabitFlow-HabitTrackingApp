package com.example.habitflow.data.remote

import com.example.habitflow.FirestorePaths
import com.example.habitflow.domain.model.Habit
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.habitflow.data.model.HabitDto


class FirestoreHabitDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun createHabit(
        habit: Habit
    ): Result<Unit> {

        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(habit.userId)
                .collection(FirestorePaths.HABITS)
                .document(habit.id)
                .set(
                    mapOf(
                        "id" to habit.id,
                        "userId" to habit.userId,
                        "name" to habit.name,
                        "description" to habit.description,
                        "frequency" to habit.frequency.name,
                        "targetDays" to habit.targetDays,
                        "isActive" to habit.isActive,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabit(
        userId: String,
        habitId: String
    ): Result<HabitDto> {

        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.HABITS)
                .document(habitId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(
                    NoSuchElementException("Habit not found")
                )
            }

            val habit = snapshot.toObject(HabitDto::class.java)
                ?: return Result.failure(
                    IllegalStateException("Unable to parse habit")
                )

            Result.success(habit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabits(
        userId: String
    ): Result<List<HabitDto>> {

        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.HABITS)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val habits = snapshot.documents.mapNotNull {
                it.toObject(HabitDto::class.java)
            }

            Result.success(habits)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHabit(
        habit: Habit
    ): Result<Unit> {

        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(habit.userId)
                .collection(FirestorePaths.HABITS)
                .document(habit.id)
                .update(
                    mapOf(
                        "name" to habit.name,
                        "description" to habit.description,
                        "frequency" to habit.frequency.name,
                        "targetDays" to habit.targetDays,
                        "isActive" to habit.isActive,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHabit(
        userId: String,
        habitId: String
    ): Result<Unit> {

        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.HABITS)
                .document(habitId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}