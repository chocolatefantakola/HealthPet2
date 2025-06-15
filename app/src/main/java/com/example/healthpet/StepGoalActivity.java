package com.example.healthpet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StepGoalActivity extends AppCompatActivity {

    private static final int STEP_GOAL = 10000;
    private static final String PREFS_NAME = "StepGoalPrefs";
    private static final String PREF_STEPS_TODAY = "stepsToday";

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;
    private SharedPreferences prefs;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        // Views holen
        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Runnable zum regelmäßigen Aktualisieren
        refreshRunnable = () -> {
            updateUI();
            handler.postDelayed(refreshRunnable, 1000);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void updateUI() {
        int stepsToday = prefs.getInt(PREF_STEPS_TODAY, 0);
        stepsTextView.setText("Steps done: " + stepsToday);
        int stepsLeft = STEP_GOAL - stepsToday;
        if (stepsLeft < 0) stepsLeft = 0;
        stepsRemainingTextView.setText("Steps remaining: " + stepsLeft);
        int progress = (int) ((stepsToday / (float) STEP_GOAL) * 100);
        if (progress > 100) progress = 100;
        stepsProgressBar.setProgress(progress);
    }
}
