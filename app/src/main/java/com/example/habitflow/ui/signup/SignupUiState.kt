package com.example.habitflow.ui.signup

data class SignupUiState(
    val name : String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
)