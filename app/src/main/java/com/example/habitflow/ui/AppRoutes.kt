package com.example.habitflow.ui


import com.example.habitflow.domain.model.HabitFrequency
import kotlinx.serialization.Serializable

sealed interface AppRoutes {


    @Serializable
   data class HabitDetailRoute( val habitId: String?=null): AppRoutes
    @Serializable
    data object LogoutRoute : AppRoutes
    @Serializable
    data object LoginRoute : AppRoutes
    @Serializable
    data object SignupRoute: AppRoutes



    @Serializable
    data object HomeRoute : AppRoutes


    @Serializable
    data class CreateHabitRoute(
     val name: String? =null,
     val description: String? =null,
     val frequency: HabitFrequency?=null ,
     val targetDays: List<Int> = emptyList()) : AppRoutes

    @Serializable
    data object AnalysisRoute : AppRoutes


}