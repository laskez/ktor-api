package com.example.database

import org.jetbrains.exposed.dao.id.IntIdTable

object TasksTable : IntIdTable("tasks") {
    val title = varchar("title", 255)
    val done = bool("done").default(false)
}

object UsersTable : IntIdTable("users") {
    val login = varchar("login", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20).default("user")
}