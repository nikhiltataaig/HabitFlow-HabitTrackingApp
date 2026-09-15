package com.example.habitflow.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.UserRepository
import com.example.habitflow.ui.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.copy


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val syncScheduler: HabitSyncScheduler
) : ViewModel(){


    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()


    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()


    fun onEvent(event: SignupScreenEvent) {

        when (event) {

            is SignupScreenEvent.onEmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.email, errorMessage = null)
            }

            is SignupScreenEvent.onPasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password, errorMessage = null)
            }

            is SignupScreenEvent.onConfirmPasswordChanged -> {
                _uiState.value = _uiState.value.copy(confirmPassword = event.password, errorMessage = null)
            }

            is SignupScreenEvent.onNameChanged->{
                _uiState.value = _uiState.value.copy(name = event.name, errorMessage = null)
            }
            SignupScreenEvent.onLoginClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.LoginRoute)
                    )
                }
            }

            SignupScreenEvent.onSignupClicked -> {
                signup()
            }
        }
    }

    fun signup() {

        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email is Required")
            viewModelScope.launch {
                _uiEvent.send(CommonUiEvent.DoNothing)
            }

            return
        }

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name is Required")
            viewModelScope.launch {
                _uiEvent.send(CommonUiEvent.DoNothing)
            }
            return
        }

        if (state.password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password should be longer than 6")
            viewModelScope.launch {
                _uiEvent.send(CommonUiEvent.DoNothing)
            }
            return
        }

        viewModelScope.launch {

           _uiEvent.send(CommonUiEvent.ShowLoader)

            val result = authRepository.signUp(
                email = state.email,
                password = state.password,
                name = state.name

            )

            if (result.isSuccess) {

                val user = result.getOrThrow()


                val userResult = userRepository.createUser(
                    user
                )

                if(userResult.isSuccess){
                    syncScheduler.scheduleInitialSync(user.id)
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.DashboardRoute))
                }




            } else {

                _uiState.value = _uiState.value.copy(errorMessage = "Error Creating a User")

                _uiEvent.send(CommonUiEvent.DoNothing)

            }


        }
    }

}