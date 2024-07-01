package com.forestspi.ritluck.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isCompleted: Boolean,
    val dueDate: Long? = null,
    val priority: Int = 0 // Add priority field
)
