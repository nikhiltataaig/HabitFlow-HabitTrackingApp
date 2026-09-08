package com.example.habitflow.data.repository

import com.example.habitflow.FirestorePaths
import com.example.habitflow.data.model.toDomain
import com.example.habitflow.data.model.toDto
import com.example.habitflow.data.remote.FirestoreUserDataSource
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await


class UserRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUserDataSource
) : UserRepository {

    override suspend fun createUser(
        user: User
    ): Result<Unit> {
        return dataSource.createUser(
            user.toDto()
        )
    }

    override suspend fun getUser(
        userId: String
    ): Result<User> {

        return dataSource
            .getUser(userId)
            .map { it.toDomain() }
    }

    override suspend fun updateUser(
        user: User
    ): Result<Unit> {

        return dataSource.updateUser(
            user.toDto()
        )
    }

    override suspend fun deleteUser(
       user: User
    ): Result<Unit> {

        return dataSource.deleteUser(user.id)
    }
}