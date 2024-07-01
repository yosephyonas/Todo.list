package com.forestspi.ritluck.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.forestspi.ritluck.data.model.Task
import com.forestspi.ritluck.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onToggleComplete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()), Filterable {

    private var originalList: List<Task> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    override fun submitList(list: List<Task>?) {
        super.submitList(list)
        list?.let {
            originalList = it
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.taskName.text = task.name
            binding.taskDueDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(task.dueDate ?: 0))

            // Set initial state without triggering listener
            binding.taskCompleted.setOnCheckedChangeListener(null)
            binding.taskCompleted.isChecked = task.isCompleted
            updateTaskNameStyle(task.isCompleted)

            // Update state and style on single click
            binding.taskCompleted.setOnCheckedChangeListener { _, isChecked ->
                onToggleComplete(task.copy(isCompleted = isChecked))
                updateTaskNameStyle(isChecked)
            }

            binding.editTask.setOnClickListener {
                onEdit(task)
            }
            binding.deleteTask.setOnClickListener {
                onDelete(task)
            }
        }

        private fun updateTaskNameStyle(isCompleted: Boolean) {
            binding.taskName.paintFlags = if (isCompleted) {
                binding.taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    originalList
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    originalList.filter {
                        it.name.lowercase(Locale.getDefault()).contains(filterPattern)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                submitList(results?.values as List<Task>?)
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
