package com.example.habitflow.ui.signup

import android.util.Log
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
import androidx.compose.ui.platform.testTag
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
fun SignupScreen(
    navController: NavController,
    signupViewModel: SignupViewModel
){

    val lifecycleOwner = LocalLifecycleOwner.current
    val showLoader = remember {mutableStateOf(false)}
    val uiState by signupViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val modifier = Modifier

    LaunchedEffect(Unit) {
        signupViewModel.uiEvent.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.CREATED).collect{ event ->
            showLoader.value = false;
            when(event){
                is CommonUiEvent.Navigate ->{
                    if (event.route is AppRoutes.HomeRoute) {
                        navController.navigate(event.route){
                            popUpTo(AppRoutes.LoginRoute){
                                inclusive = true
                            }
                        }
                    }else{
                        navController.navigate(event.route)
                    }

                }
                is CommonUiEvent.ShowToast ->
                    Toast.makeText(context,event.msg, Toast.LENGTH_SHORT).show()
                is CommonUiEvent.ShowLoader ->{ showLoader.value = true}
                is CommonUiEvent.DoNothing -> {
                    Log.d("SignupScreen","DoNothing Called")
                    showLoader.value = false
                }
                is CommonUiEvent.ShowError ->
                Toast.makeText(context,event.value, Toast.LENGTH_SHORT).show()


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
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                signupViewModel.onEvent(
                    SignupScreenEvent.onEmailChanged(it)
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
            value = uiState.name,
            onValueChange = {
                signupViewModel.onEvent(
                    SignupScreenEvent.onNameChanged(it)
                )
            },
            label = {
                Text("Name")
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
                signupViewModel.onEvent(
                    SignupScreenEvent.onPasswordChanged(it)
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
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = {
                signupViewModel.onEvent(
                    SignupScreenEvent.onConfirmPasswordChanged(it)
                )
            },
            label = {
                Text("Confirm Password")
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
                signupViewModel.onEvent(
                    SignupScreenEvent.onSignupClicked
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !showLoader.value
        ) {

            if (showLoader.value ) {

                CircularProgressIndicator()

            } else {

                Text("Create Account")
            }
        }

        TextButton(
            onClick = { signupViewModel.onEvent(SignupScreenEvent.onLoginClicked) },
            enabled = !showLoader.value
        ) {

            Text(
                "Already have an account? Login"
            )
        }

        uiState.errorMessage?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}