package com.example.healthpet;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TaskCompletion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
