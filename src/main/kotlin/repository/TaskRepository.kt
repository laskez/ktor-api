package com.example.repository

import com.example.model.Task
import java.util.concurrent.CopyOnWriteArrayList

object TaskRepository {
    private val tasks = CopyOnWriteArrayList(
        listOf(
            Task(1, "Сходить погулять"),
            Task(2, "Зайти в магазин", done = true)
        )
    )
    private var nextId = 3

    fun getAll(): List<Task> = tasks.toList()

    fun getById(id: Int): Task? = tasks.find { it.id == id }

    fun create(title: String): Task {
        val task = Task(nextId++, title)
        tasks.add(task)
        return task
    }

    fun update(id: Int, title: String?, done: Boolean?): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index == -1) return null
        val old = tasks[index]
        val updated = old.copy(
            title = title ?: old.title,
            done = done ?: old.done
        )
        tasks[index] = updated
        return updated
    }

    fun delete(id: Int): Boolean = tasks.removeIf { it.id == id }

    fun reset() {
        tasks.clear()
        tasks.addAll(
            listOf(
                Task(1, "Сходить погулять"),
                Task(2, "Зайти в магазин", done = true)
            )
        )
        nextId = 3
    }
}