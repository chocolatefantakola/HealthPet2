package com.example.healthpet.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * LogbookActivity displays a log of completed tasks grouped by date.
 * It queries the Room database for all TaskCompletion entries, formats them,
 * and displays them in a TextView.
 *
 * The logbook shows each task's name and completion time, with section headers for each date.
 * The current day is labeled as "Today".
 */
public class LogbookActivity extends AppCompatActivity {

    /**
     * The TextView where the logbook entries are displayed.
     */
    private TextView logbookTextView;

    /**
     * Called when the activity is starting. Sets up the layout and loads the task log.
     *
     * It starts a background thread to load task data from the Room database,
     * formats the data into a readable logbook string, and updates the UI on the main thread.
     * The log groups tasks by date and shows the time of completion.
     * Example entry: 📝 StepGoal at 14:35
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        logbookTextView = findViewById(R.id.logbookTextView);

        new Thread(() -> {
            // Initialize Room database and retrieve all task completions
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "task-database").build();
            List<TaskCompletion> allTasks = db.taskDao().getAllTasks();

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String lastDate = "";
            String todayStr = dateFormat.format(new Date());

            // Build logbook string, grouping tasks by date
            for (TaskCompletion task : allTasks) {
                String taskDateStr = dateFormat.format(new Date(task.completionTime));
                String dateLabel = taskDateStr.equals(todayStr) ? "Today" : taskDateStr;

                if (!taskDateStr.equals(lastDate)) {
                    sb.append("\n────── ").append(dateLabel).append(" ──────\n");
                    lastDate = taskDateStr;
                }

                sb.append("📝 ").append(task.taskName)
                        .append(" at ").append(timeFormat.format(new Date(task.completionTime)))
                        .append("\n");
            }

            // Update UI with the logbook content
            runOnUiThread(() -> logbookTextView.setText(sb.toString().trim()));
        }).start();
    }
}
