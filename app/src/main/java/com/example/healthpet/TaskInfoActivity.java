package com.example.healthpet;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskInfoActivity extends AppCompatActivity {

    private TextView taskInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        taskInfoTextView = findViewById(R.id.taskInfoTextView);

        String taskName = getIntent().getStringExtra("TASK_NAME");

        String info = "Information about " + taskName + " will be shown here.";

        taskInfoTextView.setText(info);
    }
}
