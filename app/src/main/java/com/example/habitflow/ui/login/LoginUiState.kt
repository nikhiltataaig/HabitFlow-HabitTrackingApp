package com.example.habitflow.ui.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
)