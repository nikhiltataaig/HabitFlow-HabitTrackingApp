package com.example.habitflow.ui.signup

sealed interface SignupScreenEvent {

    data class onPasswordChanged(val password : String): SignupScreenEvent

    data class onNameChanged(val name : String): SignupScreenEvent

    data class onEmailChanged(val email : String): SignupScreenEvent

    data class onConfirmPasswordChanged(val password: String):SignupScreenEvent

    data object onSignupClicked : SignupScreenEvent

    data object onLoginClicked: SignupScreenEvent

}