package com.example.habitflow.ui.analysis

sealed interface AnalysisScreenEvents {

    data object OnRefresh : AnalysisScreenEvents
}