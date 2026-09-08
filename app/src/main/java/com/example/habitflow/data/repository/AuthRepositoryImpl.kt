package com.example.habitflow.data.repository

import com.example.habitflow.data.model.UserDto
import com.example.habitflow.data.model.toDomain
import com.example.habitflow.data.remote.FirebaseAuthDataSource
import com.example.habitflow.data.remote.FirestoreUserDataSource
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val userDataSource: FirestoreUserDataSource
) : AuthRepository {

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): Result<User> {

        val authResult = authDataSource.signUp(
            email = email,
            password = password
        )

        return authResult.fold(

            onSuccess = { firebaseUser ->

                val user = UserDto(
                    id = firebaseUser.uid,
                    name = name,
                    email = email,
                    createdAt = Timestamp.now()
                )

                val firestoreResult =
                    userDataSource.createUser(user)

                firestoreResult.fold(
                    onSuccess = {
                        Result.success(user.toDomain())
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            },

            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {

        val authResult = authDataSource.login(
            email = email,
            password = password
        )

        return authResult.fold(

            onSuccess = { firebaseUser ->

                userDataSource
                    .getUser(firebaseUser.uid)
                    .map { it.toDomain() }
            },

            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override fun getCurrentUser(): User? {

        val firebaseUser =
            authDataSource.getCurrentUser()
                ?: return null

        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            createdAt = 0L
        )
    }

    override fun logout() {
        authDataSource.logout()
    }
}