package com.example

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.isAdmin(): Boolean {
    val principal = this.principal<JWTPrincipal>()
    val role = principal?.payload?.getClaim("role")?.asString()
    return role == "admin"
}