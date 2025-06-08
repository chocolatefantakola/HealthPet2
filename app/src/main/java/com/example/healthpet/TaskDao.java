package com.example.healthpet;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(TaskCompletion task);

    @Query("SELECT * FROM TaskCompletion")
    List<TaskCompletion> getAllTasks();
}
