package com.example.repository

import com.example.database.UsersTable
import com.example.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    fun findByLogin(login: String): User? = transaction {
        UsersTable.select(UsersTable.login eq login).singleOrNull()?.toUser()
    }

    fun create(login: String, passwordHash: String, role: String = "user"): User = transaction {
        val id = UsersTable.insertAndGetId {
            it[UsersTable.login] = login
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role
        }
        User(id.value, login, passwordHash, role)
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id].value,
        login = this[UsersTable.login],
        passwordHash = this[UsersTable.passwordHash],
        role = this[UsersTable.role]
    )
}