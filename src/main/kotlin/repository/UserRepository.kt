package com.example.repository

import com.example.model.User
import java.util.concurrent.CopyOnWriteArrayList

object UserRepository {
    private val users = CopyOnWriteArrayList<User>()
    private var nextId = 1

    fun findByLogin(login: String): User? = users.find { it.login == login }

    fun create(login: String, passwordHash: String): User {
        val user = User(nextId++, login, passwordHash)
        users.add(user)
        return user
    }
}