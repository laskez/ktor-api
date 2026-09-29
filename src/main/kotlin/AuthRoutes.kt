package com.example

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.model.AuthResponse
import com.example.model.ErrorResponse
import com.example.model.LoginRequest
import com.example.model.RegisterRequest
import com.example.model.RegisterResponse
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()

            if (req.login.isBlank() || req.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Логин и пароль не могут быть пустыми"))
                return@post
            }

            if (UserRepository.findByLogin(req.login) != null) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Пользователь с таким логином уже существует"))
                return@post
            }

            val passwordHash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
            val user = UserRepository.create(req.login, passwordHash)

            call.respond(HttpStatusCode.Created, RegisterResponse(user.id, user.login))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = UserRepository.findByLogin(req.login)

            if (user == null || !BCrypt.verifyer().verify(req.password.toCharArray(), user.passwordHash).verified) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Неверный логин или пароль"))
                return@post
            }

            val token = JwtConfig.generateToken(user.id, user.login)
            call.respond(AuthResponse(token))
        }
    }
}