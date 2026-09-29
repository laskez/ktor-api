package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val done: Boolean = false
)

@Serializable
data class NewTask(val title: String)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class RegisterRequest(val login: String, val password: String)

data class User(
    val id: Int,
    val login: String,
    val passwordHash: String
)

@Serializable
data class RegisterResponse(val id: Int, val login: String)

@Serializable
data class LoginRequest(val login: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val done: Boolean? = null
)