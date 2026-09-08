package com.example.habitflow.data.model

import com.google.firebase.Timestamp

data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Timestamp? = null
)