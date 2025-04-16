package com.kanbanboard.models

import android.graphics.Color

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromString(value: String): TaskPriority {
            return when (value.uppercase()) {
                "LOW" -> LOW
                "MEDIUM" -> MEDIUM
                "HIGH" -> HIGH
                else -> MEDIUM // Default priority
            }
        }
    }

    fun toDisplayString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun getColor(): Int {
        return when (this) {
            LOW -> Color.parseColor("#4CAF50")    // Green
            MEDIUM -> Color.parseColor("#FFC107")  // Yellow
            HIGH -> Color.parseColor("#F44336")    // Red
        }
    }
}
