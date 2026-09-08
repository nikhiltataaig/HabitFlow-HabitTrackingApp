package com.example.habitflow.domain.repository

import com.example.habitflow.domain.model.User

interface AuthRepository {

    suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): Result<User>

    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    fun getCurrentUser(): User?

    fun logout()
}