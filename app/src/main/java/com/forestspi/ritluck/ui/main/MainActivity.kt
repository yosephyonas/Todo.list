package com.forestspi.ritluck.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.forestspi.ritluck.R
import com.forestspi.ritluck.data.local.TaskDatabase
import com.forestspi.ritluck.data.model.Category
import com.forestspi.ritluck.data.model.Task
import com.forestspi.ritluck.data.respository.TaskRepository
import com.forestspi.ritluck.databinding.ActivityMainBinding
import com.forestspi.ritluck.ui.adapter.TaskAdapter
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    private val categories = mutableListOf(
        Category("All", R.color.defaultColor),
        Category("Food", R.color.ocean_blue),
        Category("Sport", R.color.sportColor),
        Category("Travel", R.color.travelColor),
        Category("Work", R.color.green),
        Category("Health", R.color.healthColor),
        Category("Education", R.color.educationColor),
        Category("Shopping", R.color.shoppingColor),
        Category("Entertainment", R.color.entertainmentColor),
        Category("Family", R.color.familyColor),
        Category("Friends", R.color.friendsColor),
        Category("Personal", R.color.personalColor),
        Category("Home", R.color.homeColor),
        Category("Office", R.color.officeColor)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(taskDao)
        taskViewModel = ViewModelProvider(this, TaskViewModelFactory(repository)).get(TaskViewModel::class.java)

        taskAdapter = TaskAdapter { task ->
            taskViewModel.delete(task)
        }

        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        taskViewModel.allTasks.observe(this) { tasks ->
            Log.d("MainActivity", "Tasks observed: ${tasks.size}")
            binding.tvNoTasks.isVisible = tasks.isEmpty()
            taskAdapter.submitList(tasks)
        }

        binding.fab.setOnClickListener {
            showTaskDialog(null)
        }

        setupItemTouchHelper()
        setupSearchBar()

        // Clear focus from SearchView when the app opens
        clearSearchBarFocus()
    }

    private fun setupSearchBar() {
        // Access the custom search bar EditText using view binding
        val searchEditText = binding.root.findViewById<EditText>(R.id.etSearch)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                taskAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                taskAdapter.filter.filter("")
            }
        }

        searchEditText.setOnClickListener {
            searchEditText.isFocusableInTouchMode = true
            searchEditText.requestFocus()
        }
    }

    private fun clearSearchBarFocus() {
        val searchEditText = binding.root.findViewById<EditText>(R.id.etSearch)
        searchEditText.isFocusable = false
        searchEditText.clearFocus()
        binding.dummyView.requestFocus()
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.titleTextView)
        val taskDescriptionEditText = dialogView.findViewById<EditText>(R.id.contentTextView)
        val dueDateTextView = dialogView.findViewById<TextView>(R.id.dateTextView)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val checkImageView = dialogView.findViewById<ImageView>(R.id.checkImageView)
        val cancelImageView = dialogView.findViewById<ImageView>(R.id.cancelImageView)

        val categoryNames = categories.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        categorySpinner.adapter = adapter

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        if (task != null) {
            taskNameEditText.setText(task.name)
            taskDescriptionEditText.setText(task.description)
            dueDateTextView.text = task.dueDate?.let {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(it))
            } ?: getString(R.string.select_due_date)
            val categoryPosition = adapter.getPosition(task.category)
            categorySpinner.setSelection(categoryPosition)
        }

        dueDateTextView.setOnClickListener {
            showCustomDateTimePicker(dueDateTextView)
        }

        checkImageView.setOnClickListener {
            val taskName = taskNameEditText.text.toString()
            val taskDescription = taskDescriptionEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val dueDateText = dueDateTextView.text.toString()

            if (taskName.isBlank() || taskDescription.isBlank()) {
                Snackbar.make(binding.root, "Please enter all details", Snackbar.LENGTH_LONG).show()
            } else if (dueDateText == getString(R.string.select_due_date) || dueDateText.isBlank()) {
                Snackbar.make(binding.root, "Set date and time", Snackbar.LENGTH_LONG).show()
            } else {
                val dueDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(dueDateText)?.time

                if (dueDate != null) {
                    if (task == null) {
                        Log.d("MainActivity", "Inserting new task: $taskName")
                        taskViewModel.insert(
                            Task(
                                id = 0,
                                name = taskName,
                                description = taskDescription,
                                dueDate = dueDate,
                                category = category,
                                isCompleted = false
                            )
                        )
                    } else {
                        Log.d("MainActivity", "Updating task: $taskName")
                        taskViewModel.update(task.copy(name = taskName, description = taskDescription, dueDate = dueDate, category = category))
                    }
                    builder.dismiss()
                } else {
                    Snackbar.make(binding.root, "Invalid date format", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        cancelImageView.setOnClickListener {
            builder.dismiss()
        }

        builder.show()
    }


    private fun showCustomDateTimePicker(dueDateTextView: TextView) {
        val dateTimePickerFragment = DateTimePickerBottomSheetFragment { selectedDate ->
            dueDateTextView.text = selectedDate
        }
        dateTimePickerFragment.show(supportFragmentManager, "DateTimePickerBottomSheetFragment")
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
}
