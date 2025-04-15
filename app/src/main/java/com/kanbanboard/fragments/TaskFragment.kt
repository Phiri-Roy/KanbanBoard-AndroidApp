package com.kanbanboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanbanboard.R
import com.kanbanboard.adapters.TaskAdapter
import com.kanbanboard.models.Task
import com.kanbanboard.models.TaskStatus
import com.kanbanboard.viewmodels.TaskViewModel

class TaskFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private var taskStatus: TaskStatus = TaskStatus.TODO

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: TaskStatus): TaskFragment {
            return TaskFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_STATUS)?.let {
            taskStatus = TaskStatus.valueOf(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        setupRecyclerView()
        observeTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Show task details or edit dialog
                viewModel.editTask(task)
            },
            onTaskEdit = { task ->
                // Show edit dialog
                viewModel.editTask(task)
            },
            onTaskDelete = { task ->
                showDeleteConfirmation(task)
            },
            onTaskStatusChange = { task, newStatus ->
                viewModel.updateTaskStatus(task, newStatus)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun observeTasks() {
        viewModel.getTasks(taskStatus).observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            
            // Show empty state if no tasks
            view?.findViewById<View>(R.id.emptyState)?.visibility = 
                if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.msg_confirm_delete)
            .setMessage(task.title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteTask(task)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
