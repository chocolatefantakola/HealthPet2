package com.example.healthpet.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.healthpet.model.TaskCompletion;

/**
 * AppDatabase defines the Room database for storing task completion entries.
 * It provides access to the TaskDao for database operations.
 */
@Database(entities = {TaskCompletion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Returns the TaskDao for accessing task completion data.
     *
     * @return TaskDao instance.
     */
    public abstract TaskDao taskDao();
}
