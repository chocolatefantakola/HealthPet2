package com.example.healthpet;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class StepGoalActivity extends AppCompatActivity implements SensorEventListener {

    private static final int STEP_GOAL = 10000;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;

    private SharedPreferences prefs;

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

    private static final float THRESHOLD = 11.0f;  // Schwellenwert für Erkennung
    private static final int STEP_DELAY_MS = 500;  // min. Zeit zwischen Schritten in ms
    private long lastStepTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSavedData();

        updateUI();

        resetHandler.post(resetRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_LONG).show();
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
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double magnitude = Math.sqrt(x * x + y * y + z * z);
        long currentTime = System.currentTimeMillis();

        Log.d("SensorData", "X: " + x + " Y: " + y + " Z: " + z + " Magnitude: " + magnitude);

        if (magnitude > THRESHOLD) {
            if (currentTime - lastStepTime > STEP_DELAY_MS) {
                stepsToday++;
                lastStepTime = currentTime;
                updateUI();

                if (stepsToday >= STEP_GOAL) {
                    Toast.makeText(this, "\uD83C\uDF89 Congratulations! Step goal reached! \uD83C\uDF89", Toast.LENGTH_LONG).show();
                }
            }
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
        if (progress > 100) progress = 100;
        stepsProgressBar.setProgress(progress);
    }

    private void checkAndResetDailySteps() {
        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_YEAR);
        int lastResetDay = prefs.getInt(PREF_LAST_RESET_DAY, -1);

        if (lastResetDay != today && now.get(Calendar.HOUR_OF_DAY) >= 7) {
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
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_STEPS_TODAY, stepsToday);
        editor.apply();
    }
}
