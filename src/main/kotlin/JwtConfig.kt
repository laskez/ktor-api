package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val SECRET = "super-secret-key-for-kt2-change-me-please"
    private const val ISSUER = "ktor-task-api"
    private const val VALIDITY_MS = 3600_000L

    fun generateToken(userId: Int, login: String, role: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("login", login)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }

    val verifier = JWT.require(Algorithm.HMAC256(SECRET))
        .withIssuer(ISSUER)
        .build()
}