package com.example.habitflow.ui


import kotlinx.serialization.Serializable

sealed interface AppRoutes {

    @Serializable
    data object LoginRoute : AppRoutes
    @Serializable
    data object SignupRoute: AppRoutes



    @Serializable
    data object HomeRoute : AppRoutes

}