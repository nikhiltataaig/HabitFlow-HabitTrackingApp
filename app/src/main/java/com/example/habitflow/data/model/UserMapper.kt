package com.example.habitflow.data.model


import com.example.habitflow.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        createdAt = createdAt?.toDate()?.time ?: 0L
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email
    )
}