package com.example.healthpet;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class StepGoalActivity extends AppCompatActivity implements SensorEventListener {

    private static final int STEP_GOAL = 10000;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;

    private SharedPreferences prefs;

    // To store baseline step count when activity starts (to handle step counter sensor that counts steps since device reboot)
    private int initialStepCount = -1;

    // For daily reset at 7:00
    private static final String PREFS_NAME = "StepGoalPrefs";
    private static final String PREF_LAST_RESET_DAY = "lastResetDay";
    private static final String PREF_STEPS_TODAY = "stepsToday";

    private int stepsToday;

    private Handler resetHandler = new Handler();
    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndResetDailySteps();
            resetHandler.postDelayed(this, 60 * 1000); // Check every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSavedData();

        updateUI();

        resetHandler.post(resetRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Step Counter sensor not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        saveData();
        resetHandler.removeCallbacks(resetRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (initialStepCount < 0) {
            initialStepCount = (int) event.values[0];  // baseline steps since reboot
        }

        int totalStepsSinceReboot = (int) event.values[0];
        int stepsSinceStart = totalStepsSinceReboot - initialStepCount;

        // Update today's steps counting from 7:00 AM reset
        stepsToday = stepsSinceStart;
        if (stepsToday > STEP_GOAL) stepsToday = STEP_GOAL;  // clamp max

        updateUI();

        if (stepsToday >= STEP_GOAL) {
            Toast.makeText(this, "🎉 Congratulations! Step goal reached! 🎉", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    private void updateUI() {
        stepsTextView.setText("Steps done: " + stepsToday);
        int stepsLeft = STEP_GOAL - stepsToday;
        if (stepsLeft < 0) stepsLeft = 0;
        stepsRemainingTextView.setText("Steps remaining: " + stepsLeft);

        int progress = (int) ((stepsToday / (float) STEP_GOAL) * 100);
        stepsProgressBar.setProgress(progress);
    }

    private void checkAndResetDailySteps() {
        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_YEAR);

        int lastResetDay = prefs.getInt(PREF_LAST_RESET_DAY, -1);

        if (lastResetDay != today && now.get(Calendar.HOUR_OF_DAY) >= 7) {
            // Reset step count daily at 7:00 AM
            initialStepCount = -1;
            stepsToday = 0;
            saveData();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PREF_LAST_RESET_DAY, today);
            editor.apply();

            runOnUiThread(this::updateUI);
        }
    }

    private void loadSavedData() {
        stepsToday = prefs.getInt(PREF_STEPS_TODAY, 0);
        initialStepCount = prefs.getInt("initialStepCount", -1);
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_STEPS_TODAY, stepsToday);
        editor.putInt("initialStepCount", initialStepCount);
        editor.apply();
    }
}
