package com.kanbanboard.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val darkMode: Boolean = false
) {
    // Convert to HashMap for Firestore
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "email" to email,
            "name" to name,
            "darkMode" to darkMode
        )
    }

    companion object {
        // Create User from Firestore document
        fun fromMap(map: Map<String, Any>, id: String): User {
            return User(
                id = id,
                email = map["email"] as? String ?: "",
                name = map["name"] as? String ?: "",
                darkMode = map["darkMode"] as? Boolean ?: false
            )
        }
    }
}

// Sealed class to handle user preferences updates
sealed class UserPreference {
    data class DarkMode(val enabled: Boolean) : UserPreference()
    data class NotificationsEnabled(val enabled: Boolean) : UserPreference()
    data class UpdateName(val name: String) : UserPreference()
}

// Sealed class to represent user operation results
sealed class UserResult {
    object Success : UserResult()
    data class Error(val message: String) : UserResult()
    object Loading : UserResult()
}

// Extension function to validate email
fun String.isValidEmail(): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return this.matches(emailPattern.toRegex())
}

// Extension function to validate password
fun String.isValidPassword(): Boolean {
    // Password must be at least 6 characters long
    // and contain at least one number and one letter
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$"
    return this.matches(passwordPattern.toRegex())
}

// Extension function to validate name
fun String.isValidName(): Boolean {
    // Name must be at least 2 characters long and contain only letters and spaces
    val namePattern = "^[a-zA-Z\\s]{2,}$"
    return this.matches(namePattern.toRegex())
}
