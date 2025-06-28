package com.example.healthpet.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogbookActivity extends AppCompatActivity {

    private TextView logbookTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        logbookTextView = findViewById(R.id.logbookTextView);

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "task-database").build();
            List<TaskCompletion> allTasks = db.taskDao().getAllTasks();

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String lastDate = "";
            String todayStr = dateFormat.format(new Date());

            for (TaskCompletion task : allTasks) {
                String taskDateStr = dateFormat.format(new Date(task.completionTime));
                String dateLabel = taskDateStr.equals(todayStr) ? "Today" +
                        "" +
                        "" +
                        "" : taskDateStr;

                if (!taskDateStr.equals(lastDate)) {
                    sb.append("\n──────── ").append(dateLabel).append(" ────────\n");
                    lastDate = taskDateStr;
                }

                sb.append("📝 ").append(task.taskName)
                        .append(" um ").append(timeFormat.format(new Date(task.completionTime)))
                        .append("\n");
            }

            runOnUiThread(() -> logbookTextView.setText(sb.toString().trim()));
        }).start();
    }
}
