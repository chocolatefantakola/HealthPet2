package com.example.healthpet.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * TaskCompletion represents a record of a completed task.
 * It is stored in the Room database with the task's name and completion timestamp.
 */
@Entity
public class TaskCompletion {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String taskName;

    public long completionTime;

    /**
     * Constructs a TaskCompletion entry.
     *
     * @param taskName the name of the completed task
     * @param completionTime the time of completion in milliseconds since epoch
     */
    public TaskCompletion(String taskName, long completionTime) {
        this.taskName = taskName;
        this.completionTime = completionTime;
    }
}
