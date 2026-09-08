package com.example.habitflow.data.remote

import com.example.habitflow.data.model.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.habitflow.FirestorePaths

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun createUser(user: UserDto): Result<Unit> {
        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(user.id)
                .set(user)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<UserDto> {
        return try {

            val snapshot = firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(
                    NoSuchElementException("User profile not found")
                )
            }

            val user = snapshot.toObject(UserDto::class.java)
                ?: return Result.failure(
                    IllegalStateException("Unable to parse user profile")
                )

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserDto): Result<Unit> {
        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(user.id)
                .update(
                    mapOf(
                        "name" to user.name
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {

            firestore
                .collection(FirestorePaths.USERS)
                .document(userId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}