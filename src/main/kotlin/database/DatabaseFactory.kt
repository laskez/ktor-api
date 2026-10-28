package com.example.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: DatabaseConfig) {
        Database.connect(
            url = config.url,
            driver = "org.postgresql.Driver",
            user = config.user,
            password = config.password
        )

        transaction {
            SchemaUtils.create(TasksTable, UsersTable)
        }
    }
}