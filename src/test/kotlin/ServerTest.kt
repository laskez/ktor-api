package com.example

import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ServerTest {

    @BeforeTest
    fun resetRepositories() {
        TaskRepository.reset()
        UserRepository.reset()
    }

    // Публичные маршруты

    @Test
    fun `get all tasks returns 200 and non-empty list`() = testApplication {
        application { module() }

        val response = client.get("/tasks")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Сходить погулять"))
        assertTrue(body.contains("Зайти в магазин"))
    }

    @Test
    fun `get task by id returns 200`() = testApplication {
        application { module() }

        val response = client.get("/tasks/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"id\": 1"))
    }

    @Test
    fun `get unknown task returns 404`() = testApplication {
        application { module() }

        val response = client.get("/tasks/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get task with non-numeric id returns 400`() = testApplication {
        application { module() }

        val response = client.get("/tasks/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `filter tasks by done=true`() = testApplication {
        application { module() }

        val response = client.get("/tasks?done=true")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Зайти в магазин"))
        assertFalse(body.contains("Сходить погулять"))
    }

    // Аутентификация

    @Test
    fun `register returns 201`() = testApplication {
        application { module() }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("\"login\": \"student\""))
    }

    @Test
    fun `register same login returns 409`() = testApplication {
        application { module() }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `register with empty login returns 400`() = testApplication {
        application { module() }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"","password":"123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login returns token`() = testApplication {
        application { module() }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("token"))
    }

    @Test
    fun `login with wrong password returns 401`() = testApplication {
        application { module() }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"wrong"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // Защита маршрутов

    @Test
    fun `post task without token returns 401`() = testApplication {
        application { module() }

        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Без токена"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `post task with valid token returns 201`() = testApplication {
        application { module() }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val token = extractToken(loginResp.bodyAsText())

        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"title":"С токеном"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `delete task with valid token returns 204`() = testApplication {
        application { module() }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"student","password":"pass123"}""")
        }
        val token = extractToken(loginResp.bodyAsText())

        val response = client.delete("/tasks/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    // Утилита

    private fun extractToken(json: String): String {
        val regex = """"token"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
            ?: error("Не удалось извлечь токен из ответа: $json")
    }
}