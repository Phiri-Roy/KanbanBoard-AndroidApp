package com.kanbanboard.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.kanbanboard.firebase.FirebaseHelper
import com.kanbanboard.models.Task
import com.kanbanboard.models.TaskStatus
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel : ViewModel() {
    private val firebaseHelper = FirebaseHelper()
    private var tasksListener: ListenerRegistration? = null
    
    private val _tasks = MutableLiveData<List<Task>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _error = MutableLiveData<String?>()
    private val _selectedTask = MutableLiveData<Task?>()

    val isLoading: LiveData<Boolean> = _isLoading
    val error: LiveData<String?> = _error
    val selectedTask: LiveData<Task?> = _selectedTask

    init {
        startListeningToTasks()
    }

    private fun startListeningToTasks() {
        tasksListener = firebaseHelper.getUserTasks()
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }

                val taskList = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        Task.fromMap(data, doc.id)
                    }
                } ?: emptyList()

                _tasks.value = taskList
            }
    }

    fun getTasks(status: TaskStatus): LiveData<List<Task>> {
        val filteredTasks = MutableLiveData<List<Task>>()
        
        _tasks.observeForever { tasks ->
            filteredTasks.value = tasks.filter { it.status == status }
        }
        
        return filteredTasks
    }

    fun createTask(
        title: String,
        description: String,
        dueDate: Date?,
        priority: TaskPriority,
        status: TaskStatus = TaskStatus.TODO
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = firebaseHelper.getCurrentUserId()
                    ?: throw IllegalStateException("User not authenticated")

                val task = Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = status,
                    userId = userId,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                firebaseHelper.createTask(task)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseHelper.updateTask(task)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: TaskStatus) {
        val updatedTask = task.copy(
            status = newStatus,
            updatedAt = Date()
        )
        updateTask(updatedTask)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseHelper.deleteTask(task.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editTask(task: Task) {
        _selectedTask.value = task
    }

    fun clearSelectedTask() {
        _selectedTask.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }
}
