package com.example.habitflow.ui.login

sealed interface LoginScreenEvents {

    data class onPasswordChanged(val password : String): LoginScreenEvents

    data class onEmailChanged(val email : String): LoginScreenEvents

    data object onSignupClicked : LoginScreenEvents

    data object onLoginClicked: LoginScreenEvents


}