package com.example.habitflow.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long
)