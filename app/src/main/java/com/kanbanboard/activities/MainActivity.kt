package com.kanbanboard.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.kanbanboard.R
import com.kanbanboard.databinding.ActivityMainBinding
import com.kanbanboard.databinding.DialogTaskBinding
import com.kanbanboard.firebase.FirebaseHelper
import com.kanbanboard.fragments.TaskFragment
import com.kanbanboard.models.Task
import com.kanbanboard.models.TaskPriority
import com.kanbanboard.models.TaskStatus
import com.kanbanboard.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private val firebaseHelper = FirebaseHelper()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupViewPager() {
        // Setup ViewPager
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = TaskStatus.values().size
            override fun createFragment(position: Int) = 
                TaskFragment.newInstance(TaskStatus.values()[position])
        }

        // Setup TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TaskStatus.values()[position].toDisplayString()
        }.attach()
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.selectedTask.observe(this) { task ->
            task?.let {
                showTaskDialog(it)
                viewModel.clearSelectedTask()
            }
        }
    }

    private fun showTaskDialog(task: Task? = null) {
        val dialogBinding = DialogTaskBinding.inflate(layoutInflater)
        var selectedDate: Date? = task?.dueDate
        
        // Setup status spinner if editing
        if (task != null) {
            dialogBinding.tilStatus.visibility = View.VISIBLE
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                TaskStatus.values().map { it.toDisplayString() }
            )
            (dialogBinding.spinnerStatus as? AutoCompleteTextView)?.setAdapter(adapter)
            dialogBinding.spinnerStatus.setText(task.status.toDisplayString(), false)
        }

        // Set existing values if editing
        dialogBinding.apply {
            etTaskTitle.setText(task?.title)
            etTaskDescription.setText(task?.description)
            etDueDate.setText(task?.dueDate?.let { dateFormat.format(it) })

            // Set priority
            when (task?.priority) {
                TaskPriority.LOW -> rbLow.isChecked = true
                TaskPriority.MEDIUM -> rbMedium.isChecked = true
                TaskPriority.HIGH -> rbHigh.isChecked = true
                null -> rbMedium.isChecked = true
            }

            // Setup due date picker
            etDueDate.setOnClickListener {
                showDatePicker { date ->
                    selectedDate = date
                    etDueDate.setText(dateFormat.format(date))
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (task == null) R.string.btn_add_task else R.string.btn_update_task)
            .setView(dialogBinding.root)
            .setPositiveButton(if (task == null) R.string.btn_add_task else R.string.btn_update_task) { _, _ ->
                val title = dialogBinding.etTaskTitle.text.toString().trim()
                val description = dialogBinding.etTaskDescription.text.toString().trim()
                val priority = when {
                    dialogBinding.rbLow.isChecked -> TaskPriority.LOW
                    dialogBinding.rbHigh.isChecked -> TaskPriority.HIGH
                    else -> TaskPriority.MEDIUM
                }

                if (title.isEmpty()) {
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (task == null) {
                    // Create new task
                    viewModel.createTask(
                        title = title,
                        description = description,
                        dueDate = selectedDate,
                        priority = priority
                    )
                } else {
                    // Update existing task
                    val status = TaskStatus.values().first { 
                        it.toDisplayString() == dialogBinding.spinnerStatus.text.toString() 
                    }
                    viewModel.updateTask(
                        task.copy(
                            title = title,
                            description = description,
                            dueDate = selectedDate,
                            priority = priority,
                            status = status,
                            updatedAt = Date()
                        )
                    )
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_dark_mode -> {
                toggleDarkMode()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleDarkMode() {
        val newMode = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(AuthActivity::class.java)
        finish()
    }

    private fun startActivity(activityClass: Class<*>) {
        startActivity(android.content.Intent(this, activityClass))
    }
}
