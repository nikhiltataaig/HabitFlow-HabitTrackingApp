package com.example.habitflow.ui


import kotlinx.serialization.Serializable

sealed interface AppRoutes {


    @Serializable
    data object LogoutRoute : AppRoutes
    @Serializable
    data object LoginRoute : AppRoutes
    @Serializable
    data object SignupRoute: AppRoutes



    @Serializable
    data object HomeRoute : AppRoutes


    @Serializable
    data object CreateHabitRoute : AppRoutes

    @Serializable
    data object AnalysisRoute : AppRoutes


}