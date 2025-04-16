package com.kanbanboard.models

import com.google.firebase.Timestamp
import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Date? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val userId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    // Convert to HashMap for Firestore
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "dueDate" to dueDate?.let { Timestamp(it) },
            "status" to status.name,
            "priority" to priority.name,
            "userId" to userId,
            "createdAt" to Timestamp(createdAt),
            "updatedAt" to Timestamp(updatedAt)
        )
    }

    companion object {
        // Create Task from Firestore document
        fun fromMap(map: Map<String, Any>, id: String): Task {
            return Task(
                id = id,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                dueDate = (map["dueDate"] as? Timestamp)?.toDate(),
                status = TaskStatus.valueOf(map["status"] as? String ?: TaskStatus.TODO.name),
                priority = TaskPriority.valueOf(map["priority"] as? String ?: TaskPriority.MEDIUM.name),
                userId = map["userId"] as? String ?: "",
                createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date()
            )
        }
    }
}
