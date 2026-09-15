package com.example.habitflow.data.remote

import com.example.habitflow.FirestorePaths
import com.example.habitflow.domain.model.HabitCompletion
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import com.example.habitflow.data.model.HabitCompletionDto
import com.google.firebase.Timestamp
import java.util.Date
import javax.inject.Inject


class FirestoreCompletionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun createCompletion(
        completion: HabitCompletion
    ): Result<Unit> {

        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(completion.userId)
                .collection(FirestorePaths.COMPLETIONS)
                .document(completion.id)
                .set(
                    mapOf(
                        "id" to completion.id,
                        "userId" to completion.userId,
                        "habitId" to completion.habitId,
                        "date" to completion.date,
                        "completedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompletion(
        userId: String,
        completionId: String
    ): Result<HabitCompletionDto> {

        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.COMPLETIONS)
                .document(completionId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(
                    NoSuchElementException("Completion not found")
                )
            }

            val completion =
                snapshot.toObject(HabitCompletionDto::class.java)
                    ?: return Result.failure(
                        IllegalStateException(
                            "Unable to parse completion"
                        )
                    )

            Result.success(completion)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompletionsForHabit(
        userId: String,
        habitId: String
    ): Result<List<HabitCompletionDto>> {

        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.COMPLETIONS)
                .whereEqualTo("habitId", habitId)
                .get()
                .await()

            val completions = snapshot.documents.mapNotNull {
                it.toObject(HabitCompletionDto::class.java)
            }

            Result.success(completions)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCompletions(
        userId: String
    ): Result<List<HabitCompletionDto>> {

        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.COMPLETIONS)
                .get()
                .await()

            val completions = snapshot.documents.mapNotNull {
                it.toObject(HabitCompletionDto::class.java)
            }

            Result.success(completions)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCompletion(
        userId: String,
        completionId: String
    ): Result<Unit> {

        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .collection(FirestorePaths.COMPLETIONS)
                .document(completionId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun syncCompletion(
        completion: HabitCompletion
    ): Result<Unit> {
        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(completion.userId)
                .collection(FirestorePaths.COMPLETIONS)
                .document(completion.id)
                .set(
                    mapOf(
                        "id" to completion.id,
                        "userId" to completion.userId,
                        "habitId" to completion.habitId,
                        "date" to completion.date,
                        "completedAt" to Timestamp(
                            Date(completion.completedAt)
                        )
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}