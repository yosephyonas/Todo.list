package com.forestspi.ritluck.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.forestspi.ritluck.data.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY priority DESC, dueDate ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
