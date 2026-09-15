package com.example.habitflow.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.data.repository.AuthRepositoryImpl
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.copy
import com.example.habitflow.ui.AppRoutes
import javax.inject.Inject
import kotlin.collections.copy


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncScheduler: HabitSyncScheduler
) : ViewModel(){



    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()



    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)

    val uiEvent = _uiEvent.receiveAsFlow()


    fun onEvent(event: LoginScreenEvents){

        when (event) {
            is LoginScreenEvents.onEmailChanged -> {
                _uiState.update { it.copy(email = event.email, errorMessage = null) }
            }
            is LoginScreenEvents.onPasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password, errorMessage = null)
            }
            LoginScreenEvents.onLoginClicked -> {
                login()
            }
            LoginScreenEvents.onSignupClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.SignupRoute))
                }
            }
        }
    }


    private fun login(){

        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy( errorMessage = null)
            _uiEvent.send(CommonUiEvent.ShowLoader)

            val result = authRepository.login(email,password)

            if(result.isSuccess){
                _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.DashboardRoute))

                val user = result.getOrThrow()
                syncScheduler.scheduleInitialSync(user.id)

            }
            else{
                _uiState.value = _uiState.value.copy(errorMessage = "Login Failed")
                _uiEvent.send(CommonUiEvent.DoNothing)
            }
        }





    }


}