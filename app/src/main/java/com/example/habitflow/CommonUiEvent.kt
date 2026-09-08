package com.example.habitflow

import android.content.Context
import com.example.habitflow.ui.AppRoutes


sealed interface CommonUiEvent {
    data class ShowToast(val msg: String): CommonUiEvent

    data class Navigate(val route : AppRoutes):CommonUiEvent

    data object PopBackStack: CommonUiEvent
    data class ExecuteWithContext(val callback:(context:Context) -> Unit):CommonUiEvent

    data class ShowError(val value: String): CommonUiEvent

    data object ShowLoader: CommonUiEvent

    data object DoNothing: CommonUiEvent
}