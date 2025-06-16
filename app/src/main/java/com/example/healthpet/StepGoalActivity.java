package com.example.healthpet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StepGoalActivity extends AppCompatActivity {

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;

    private int dailyStepGoal = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        stepsProgressBar.setMax(dailyStepGoal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSteps();
    }

    private void loadSteps() {
        SharedPreferences prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE);
        int stepsToday = prefs.getInt("stepsToday", 0);

        stepsTextView.setText("Steps done: " + stepsToday);
        stepsRemainingTextView.setText("Steps remaining: " + (dailyStepGoal - stepsToday));
        stepsProgressBar.setProgress(stepsToday);
    }
}
