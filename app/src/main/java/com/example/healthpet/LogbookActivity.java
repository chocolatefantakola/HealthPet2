package com.example.healthpet;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LogbookActivity extends AppCompatActivity {

    private TextView logbookTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        logbookTextView = findViewById(R.id.logbookTextView);

        new Thread(() -> {
            // Room-Datenbank öffnen
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "task-database").build();
            List<TaskCompletion> allTasks = db.taskDao().getAllTasks();

            // Ergebnis formatieren
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            for (TaskCompletion task : allTasks) {
                sb.append("📝 ")
                        .append(task.taskName)
                        .append(" erledigt am ")
                        .append(sdf.format(new Date(task.completionTime)))
                        .append("\n");
            }

            // Ausgabe im UI-Thread anzeigen
            runOnUiThread(() -> logbookTextView.setText(sb.toString()));
        }).start();
    }
}
