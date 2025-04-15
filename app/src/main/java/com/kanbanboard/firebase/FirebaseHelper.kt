package com.kanbanboard.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.kanbanboard.models.Task as KanbanTask
import com.kanbanboard.models.User
import com.kanbanboard.models.UserResult
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TASKS_COLLECTION = "tasks"
    }

    // Authentication Methods
    suspend fun signIn(email: String, password: String): UserResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            UserResult.Success
        } catch (e: Exception) {
            UserResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signUp(email: String, password: String, name: String): UserResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Create user document in Firestore
                createUserDocument(user.uid, email, name)
            }
            UserResult.Success
        } catch (e: Exception) {
            UserResult.Error(e.message ?: "Sign up failed")
        }
    }

    private suspend fun createUserDocument(userId: String, email: String, name: String) {
        val user = User(userId, email, name)
        db.collection(USERS_COLLECTION).document(userId)
            .set(user.toMap())
            .await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): UserResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            UserResult.Success
        } catch (e: Exception) {
            UserResult.Error(e.message ?: "Password reset failed")
        }
    }

    // User Methods
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()
            
            if (document.exists()) {
                User.fromMap(document.data!!, firebaseUser.uid)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserPreference(preference: Map<String, Any>): UserResult {
        val userId = auth.currentUser?.uid ?: return UserResult.Error("User not authenticated")
        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(preference)
                .await()
            UserResult.Success
        } catch (e: Exception) {
            UserResult.Error(e.message ?: "Update failed")
        }
    }

    // Task Methods
    suspend fun createTask(task: KanbanTask): Result<String> {
        return try {
            val taskMap = task.toMap()
            val docRef = db.collection(TASKS_COLLECTION)
                .add(taskMap)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(task: KanbanTask): Result<Unit> {
        return try {
            val taskMap = task.toMap().toMutableMap()
            taskMap["updatedAt"] = Date()
            
            db.collection(TASKS_COLLECTION)
                .document(task.id)
                .update(taskMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            db.collection(TASKS_COLLECTION)
                .document(taskId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserTasks(): Query {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        return db.collection(TASKS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("dueDate", Query.Direction.ASCENDING)
    }

    // FCM Token Management
    suspend fun updateFCMToken() {
        try {
            val token = messaging.token.await()
            val userId = auth.currentUser?.uid ?: return
            
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Handle token update failure
        }
    }

    // Helper Methods
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
