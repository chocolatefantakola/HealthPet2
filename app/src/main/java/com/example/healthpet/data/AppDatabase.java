package com.example.healthpet.data;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.healthpet.model.TaskCompletion;

@Database(entities = {TaskCompletion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
