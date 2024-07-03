package com.forestspi.ritluck.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.forestspi.ritluck.R
import com.forestspi.ritluck.data.model.Task
import com.forestspi.ritluck.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onDelete: (Task) -> Unit
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
        originalList = list ?: listOf()
        super.submitList(originalList)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.taskTime.text = task.dueDate?.let {
                SimpleDateFormat("HH:mm", Locale.US).format(Date(it))
            }
            binding.taskName.text = task.name
            binding.taskDescription.text = task.description

            val background = binding.root.background as GradientDrawable
            background.setColor(ContextCompat.getColor(binding.root.context, getCategoryColor(task.category)))

            binding.deleteTask.setOnClickListener {
                onDelete(task)
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category) {
                "Sport" -> R.color.sportColor
                "Food" -> R.color.ocean_blue
                "Travel" -> R.color.travelColor
                "Work" -> R.color.workColor
                "Health" -> R.color.healthColor
                "Education" -> R.color.educationColor
                "Shopping" -> R.color.shoppingColor
                "Entertainment" -> R.color.entertainmentColor
                "Family" -> R.color.familyColor
                "Friends" -> R.color.friendsColor
                "Personal" -> R.color.personalColor
                "Home" -> R.color.homeColor
                "Office" -> R.color.officeColor
                else -> R.color.defaultColor
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
                        it.name.lowercase(Locale.getDefault()).contains(filterPattern) ||
                                it.description.lowercase(Locale.getDefault()).contains(filterPattern)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                super@TaskAdapter.submitList(results?.values as List<Task>)
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
