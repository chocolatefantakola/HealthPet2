package com.example.healthpet.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.healthpet.model.TaskCompletion;

import java.util.List;

/**
 * TaskDao provides database operations for TaskCompletion entities.
 * It allows inserting new tasks and querying existing records.
 */
@Dao
public interface TaskDao {

    /**
     * Inserts a new task completion record into the database.
     *
     * @param task The TaskCompletion entity to insert.
     */
    @Insert
    void insert(TaskCompletion task);

    /**
     * Retrieves all task completion records from the database.
     *
     * @return List of all TaskCompletion entries.
     */
    @Query("SELECT * FROM TaskCompletion")
    List<TaskCompletion> getAllTasks();

    /**
     * Checks if a task with the given name was completed after the specified time.
     *
     * @param taskName The name of the task to check.
     * @param fromMillis The time in milliseconds to compare against.
     * @return true if such a task exists, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM TaskCompletion WHERE taskName = :taskName AND completionTime >= :fromMillis)")
    boolean isTaskCompletedAfter(String taskName, long fromMillis);
}
