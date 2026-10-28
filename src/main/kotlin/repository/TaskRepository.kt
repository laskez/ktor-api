package com.example.repository

import com.example.database.TasksTable
import com.example.model.Task
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object TaskRepository {

    fun getAll(): List<Task> = transaction {
        TasksTable.selectAll().map { it.toTask() }
    }

    fun getById(id: Int): Task? = transaction {
        TasksTable.select(TasksTable.id eq id).singleOrNull()?.toTask()
    }

    fun create(title: String): Task = transaction {
        val id = TasksTable.insertAndGetId {
            it[TasksTable.title] = title
            it[done] = false
        }
        Task(id.value, title, false)
    }

    fun update(id: Int, title: String?, done: Boolean?): Task? = transaction {
        val existing = TasksTable.select(TasksTable.id eq id).singleOrNull() ?: return@transaction null
        TasksTable.update({ TasksTable.id eq id }) {
            if (title != null) it[TasksTable.title] = title
            if (done != null) it[TasksTable.done] = done
        }
        Task(id, title ?: existing[TasksTable.title], done ?: existing[TasksTable.done])
    }

    fun delete(id: Int): Boolean = transaction {
        TasksTable.deleteWhere { TasksTable.id eq id } > 0
    }

    private fun ResultRow.toTask() = Task(
        id = this[TasksTable.id].value,
        title = this[TasksTable.title],
        done = this[TasksTable.done]
    )
}