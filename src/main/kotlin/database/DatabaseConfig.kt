package com.example.database

import io.ktor.server.config.*

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String
) {
    companion object {
        fun from(config: ApplicationConfig): DatabaseConfig {
            val envUrl = System.getenv("DATABASE_URL")
            val envUser = System.getenv("DATABASE_USER")
            val envPass = System.getenv("DATABASE_PASSWORD")

            return if (envUrl != null && envUser != null && envPass != null) {
                DatabaseConfig(envUrl, envUser, envPass)
            } else {
                DatabaseConfig(
                    url = config.property("database.url").getString(),
                    user = config.property("database.user").getString(),
                    password = config.property("database.password").getString()
                )
            }
        }
    }
}