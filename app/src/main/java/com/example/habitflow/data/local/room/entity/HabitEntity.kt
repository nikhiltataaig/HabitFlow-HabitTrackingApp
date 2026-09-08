package com.example.habitflow.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits"
)
data class HabitEntity(

    @PrimaryKey
    val id: String,

    val userId: String,

    val name: String,

    val description: String,

    val frequency: String,

    val targetDays: List<Int>,

    val isActive: Boolean,

    val createdAt: Long,

    val updatedAt: Long
)