package com.example.habitflow.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signUp(
        email: String,
        password: String
    ): Result<FirebaseUser> {

        return try {

            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: return Result.failure(
                    IllegalStateException("User creation failed")
                )

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {

        return try {

            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val user = result.user
                ?: return Result.failure(
                    IllegalStateException("Login failed")
                )

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}