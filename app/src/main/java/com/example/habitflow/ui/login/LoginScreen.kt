package com.example.habitflow.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.ui.AppRoutes


@Composable
fun LoginScreen (
    navController: NavController,
    loginViewModel: LoginViewModel ){


    val modifier = Modifier
    val showLoader = remember { mutableStateOf(false) };
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        loginViewModel.uiEvent.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                showLoader.value = false

                when (event) {
                    is CommonUiEvent.Navigate -> {
                        if (event.route is AppRoutes.HomeRoute) {
                            navController.navigate(event.route) {
                                popUpTo(AppRoutes.LoginRoute) {
                                    inclusive = true
                                }
                            }
                        } else {
                            navController.navigate(event.route)
                        }

                    }

                    is CommonUiEvent.ShowToast ->
                        Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()

                    is CommonUiEvent.ShowLoader -> {
                        showLoader.value = true
                    }


                    else -> {}
                }
            }
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Login to continue chatting."
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                loginViewModel.onEvent(
                    LoginScreenEvents.onEmailChanged(it)
                )
            },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !showLoader.value
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = {
                loginViewModel.onEvent(
                    LoginScreenEvents.onPasswordChanged(it)
                )
            },
            label = {
                Text("Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !showLoader.value
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                loginViewModel.onEvent(
                    LoginScreenEvents.onLoginClicked
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !showLoader.value
        ) {

            if (showLoader.value) {

                CircularProgressIndicator()

            } else {

                Text("Login")
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        TextButton(
            onClick = { loginViewModel.onEvent(LoginScreenEvents.onSignupClicked) },
            enabled = !showLoader.value
        ) {

            Text(
                "Don't have an account? Sign up"
            )
        }

        uiState.errorMessage?.let { error ->

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }







}