package com.kanbanboard.models

import com.kanbanboard.R

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    fun toDisplayString(): String {
        return when (this) {
            TODO -> "To Do"
            IN_PROGRESS -> "In Progress"
            DONE -> "Done"
        }
    }

    fun getThemeAttribute(): Int {
        return when (this) {
            TODO -> R.attr.taskTodoColor
            IN_PROGRESS -> R.attr.taskInProgressColor
            DONE -> R.attr.taskDoneColor
        }
    }
}
