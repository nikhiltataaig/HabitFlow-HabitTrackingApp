package com.example.habitflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habitflow.ui.home.HomeScreen
import com.example.habitflow.ui.login.LoginScreen
import com.example.habitflow.ui.login.LoginViewModel
import com.example.habitflow.ui.signup.SignupScreen
import com.example.habitflow.ui.signup.SignupViewModel


@Composable
fun AppNavigation(modifier: Modifier) {


    val navController = rememberNavController()

    NavHost(navController = navController , startDestination = AppRoutes.LoginRoute){

        composable<AppRoutes.LoginRoute>{
            val loginViewModel: LoginViewModel = hiltViewModel()

            LoginScreen(navController,loginViewModel)



        }

        composable<AppRoutes.SignupRoute>{

            val signupViewModel : SignupViewModel = hiltViewModel()
            SignupScreen(navController,signupViewModel)
        }

        composable<AppRoutes.HomeRoute> {

            HomeScreen(navController)
        }

    }

}