package com.example.todoapp.data

class TaskRepository(private val dao: TaskDao) {

    val allTasks = dao.getAllTasks()

    suspend fun insert(task: Task) {
        dao.insert(task)
    }

    suspend fun update(task: Task) {
        dao.update(task)
    }

    suspend fun delete(task: Task) {
        dao.delete(task)
    }
}
