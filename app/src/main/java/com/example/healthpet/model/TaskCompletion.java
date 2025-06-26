package com.example.healthpet.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TaskCompletion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String taskName;
    public long completionTime;

    public TaskCompletion(String taskName, long completionTime) {
        this.taskName = taskName;
        this.completionTime = completionTime;
    }
}

