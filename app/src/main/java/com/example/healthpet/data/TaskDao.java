package com.example.healthpet.data;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.healthpet.model.TaskCompletion;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(TaskCompletion task);

    @Query("SELECT * FROM TaskCompletion")
    List<TaskCompletion> getAllTasks();

    @Query("SELECT EXISTS(SELECT 1 FROM TaskCompletion WHERE taskName = :taskName AND completionTime >= :fromMillis)")
    boolean isTaskCompletedAfter(String taskName, long fromMillis);

}
