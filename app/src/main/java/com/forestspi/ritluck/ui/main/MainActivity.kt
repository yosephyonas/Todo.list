package com.forestspi.ritluck.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.forestspi.ritluck.R
import com.forestspi.ritluck.data.local.TaskDatabase
import com.forestspi.ritluck.data.model.Task
import com.forestspi.ritluck.data.repository.TaskRepository
import com.forestspi.ritluck.databinding.ActivityMainBinding
import com.forestspi.ritluck.ui.adapter.TaskAdapter
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(taskDao)
        taskViewModel = ViewModelProvider(this, TaskViewModelFactory(repository)).get(TaskViewModel::class.java)

        taskAdapter = TaskAdapter(
            onEdit = { showTaskDialog(it) },
            onDelete = { task -> taskViewModel.delete(task) },
            onToggleComplete = { task ->
                taskViewModel.update(task)
            }
        )

        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        taskViewModel.allTasks.observe(this, { tasks ->
            binding.tvNoTasks.isVisible = tasks.isEmpty()
            taskAdapter.submitList(tasks)
        })

        binding.btnAddTask.setOnClickListener {
            showTaskDialog(null)
        }

        setupItemTouchHelper()
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.etTaskName)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val dueDateTextView = dialogView.findViewById<TextView>(R.id.tvDueDate)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val taskName = taskNameEditText.text.toString()
                val priority = prioritySpinner.selectedItemPosition
                val dueDateText = dueDateTextView.text.toString()

                if (dueDateText == getString(R.string.select_due_date)) {
                    Snackbar.make(binding.root, "Set date and time", Snackbar.LENGTH_LONG).show()
                } else {
                    val dueDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(dueDateText)?.time

                    if (task == null) {
                        taskViewModel.insert(Task(id = 0, name = taskName, priority = priority, dueDate = dueDate, isCompleted = false))
                    } else {
                        taskViewModel.update(task.copy(name = taskName, priority = priority, dueDate = dueDate))
                    }
                }
            }
            .setNegativeButton("Cancel", null)

        if (task != null) {
            taskNameEditText.setText(task.name)
            prioritySpinner.setSelection(task.priority)
            dueDateTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(task.dueDate ?: 0))
        }

        dueDateTextView.setOnClickListener {
            showDateTimePickerDialog(dueDateTextView)
        }

        builder.create().show()
    }

    private fun showDateTimePickerDialog(dueDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(calendar.time)
                dueDateTextView.text = formattedDate
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                taskAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = taskAdapter.currentList[viewHolder.adapterPosition]
                taskViewModel.delete(task)
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        taskViewModel.insert(task)
                    }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search tasks..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskAdapter.filter.filter(newText)
                return true
            }
        })
        return true
    }
}
