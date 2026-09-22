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