package com.kanbanboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kanbanboard.R
import com.kanbanboard.models.Task
import com.kanbanboard.models.TaskStatus
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskStatusChange: (Task, TaskStatus) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val viewPriorityIndicator: View = itemView.findViewById(R.id.viewPriorityIndicator)
        private val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDueDate.text = task.dueDate?.let { "Due: ${dateFormat.format(it)}" } ?: "No due date"
            tvPriority.text = task.priority.toDisplayString()
            tvPriority.setBackgroundColor(task.priority.getColor())
            viewPriorityIndicator.setBackgroundColor(task.priority.getColor())

            // Set click listener for the whole item
            itemView.setOnClickListener { onTaskClick(task) }

            // Set up the popup menu
            menuButton.setOnClickListener { view ->
                showPopupMenu(view, task)
            }
        }

        private fun showPopupMenu(view: View, task: Task) {
            PopupMenu(view.context, view).apply {
                inflate(R.menu.task_menu)
                
                // Add status change submenu items dynamically
                menu.findItem(R.id.action_change_status)?.subMenu?.apply {
                    TaskStatus.values().forEach { status ->
                        if (status != task.status) {
                            add(status.toDisplayString()).setOnMenuItemClickListener {
                                onTaskStatusChange(task, status)
                                true
                            }
                        }
                    }
                }

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onTaskEdit(task)
                            true
                        }
                        R.id.action_delete -> {
                            onTaskDelete(task)
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
