package com.example.habitflow

object FirestorePaths {

    const val USERS = "users"
    const val HABITS = "habits"
    const val COMPLETIONS = "completions"

    fun user(userId: String) =
        "$USERS/$userId"

    fun habits(userId: String) =
        "$USERS/$userId/$HABITS"

    fun habit(
        userId: String,
        habitId: String
    ) =
        "$USERS/$userId/$HABITS/$habitId"

    fun completions(userId: String) =
        "$USERS/$userId/$COMPLETIONS"
}