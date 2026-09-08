package com.example.habitflow.domain.repository

import com.example.habitflow.domain.model.User

interface UserRepository {

    suspend fun getUser(
        userId: String
    ): Result<User>

    suspend fun createUser(
        user: User
    ): Result<Unit>

    suspend fun updateUser(
        user: User
    ): Result<Unit>


    suspend fun deleteUser(user : User) :
            Result<Unit>
}