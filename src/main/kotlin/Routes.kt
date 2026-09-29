package com.example

import com.example.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val tasks = mutableListOf(
    Task(1, "Сходить погулять"),
    Task(2, "Зайти в магазин", done = true)
)
private var nextId = 3

fun Application.configureRoutes() {
    routing {
        authRoutes()

        route("/tasks") {

            // Публичные
            get {
                val doneParam = call.request.queryParameters["done"]
                when (doneParam) {
                    null -> call.respond(tasks)
                    "true" -> call.respond(tasks.filter { it.done })
                    "false" -> call.respond(tasks.filter { !it.done })
                    else -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Параметр 'done' должен быть true или false")
                    )
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                    return@get
                }
                val task = tasks.find { it.id == id }
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Задача с id=$id не найдена"))
                } else {
                    call.respond(task)
                }
            }

            // Защищенные
            authenticate("auth-jwt") {

                post {
                    val newTask = try {
                        call.receive<NewTask>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный JSON"))
                        return@post
                    }
                    if (newTask.title.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Поле 'title' не может быть пустым"))
                        return@post
                    }
                    val created = Task(id = nextId++, title = newTask.title)
                    tasks.add(created)
                    call.respond(HttpStatusCode.Created, created)
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                        return@put
                    }
                    val req = try {
                        call.receive<UpdateTaskRequest>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный JSON"))
                        return@put
                    }
                    val index = tasks.indexOfFirst { it.id == id }
                    if (index == -1) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Задача с id=$id не найдена"))
                        return@put
                    }
                    val old = tasks[index]
                    val updated = old.copy(
                        title = req.title ?: old.title,
                        done = req.done ?: old.done
                    )
                    tasks[index] = updated
                    call.respond(updated)
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                        return@delete
                    }
                    val removed = tasks.removeIf { it.id == id }
                    if (removed) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Задача с id=$id не найдена"))
                    }
                }
            }
        }
    }
}