package com.example

import com.example.model.*
import com.example.repository.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        authRoutes()

        route("/tasks") {

            // Публичные
            get {
                val doneParam = call.request.queryParameters["done"]
                val all = TaskRepository.getAll()
                when (doneParam) {
                    null -> call.respond(all)
                    "true" -> call.respond(all.filter { it.done })
                    "false" -> call.respond(all.filter { !it.done })
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
                val task = TaskRepository.getById(id)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Задача с id=$id не найдена"))
                } else {
                    call.respond(task)
                }
            }

            // Защищенные
            authenticate("auth-jwt") {

                post {
                    val newTask = call.receive<NewTask>()
                    if (newTask.title.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Поле 'title' не может быть пустым"))
                        return@post
                    }
                    val created = TaskRepository.create(newTask.title)
                    call.respond(HttpStatusCode.Created, created)
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                        return@put
                    }
                    val req = call.receive<UpdateTaskRequest>()
                    val updated = TaskRepository.update(id, req.title, req.done)
                    if (updated == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Задача с id=$id не найдена"))
                    } else {
                        call.respond(updated)
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                        return@delete
                    }
                    val removed = TaskRepository.delete(id)
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