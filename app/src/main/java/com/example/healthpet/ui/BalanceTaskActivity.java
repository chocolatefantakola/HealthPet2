package com.example.healthpet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.util.Calendar;

/**
 * BalanceTaskActivity handles the balance exercise using the accelerometer sensor.
 * It guides the user through right and left leg balancing tasks and tracks completion.
 */
public class BalanceTaskActivity extends AppCompatActivity implements SensorEventListener {

    // Sensor and UI components
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private TextView timerText;
    private Button startButton;
    private BalanceView balanceView;
    private TextView mainText;
    private ImageView infoIcon;

    private enum Stage { READY, COUNTDOWN, RIGHT_LEG, REST, LEFT_LEG, DONE }

    private Stage currentStage = Stage.READY;
    private boolean isBalancing = false;
    private static final int BALANCE_DURATION = 10000;
    private CountDownTimer balanceTimer;

    private float filteredX = 0;
    private float filteredY = 0;
    private static final float ALPHA = 0.3f;

    private static final String PREFS_NAME = "BalancePrefs";
    private static final String KEY_LAST_DONE = "lastBalanceDone";

    /**
     * Initializes the activity, views, sensor, and logic.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_task);

        // Initialize views
        mainText = findViewById(R.id.mainText);
        infoIcon = findViewById(R.id.infoIcon);
        timerText = findViewById(R.id.timerText);
        startButton = findViewById(R.id.startButton);
        balanceView = findViewById(R.id.balanceView);

        infoIcon.setOnClickListener(v -> showInstructionDialog());

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometerSensor == null) {
            Toast.makeText(this, "No accelerometer available!", Toast.LENGTH_SHORT).show();
        }

        balanceView.setBalanceFailListener(() -> {
            if (isBalancing) {
                runOnUiThread(this::showFailDialog);
            }
        });

        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            startButton.setOnClickListener(v -> startSession());
        }
    }

    /**
     * Starts the balance task session with countdown and balance phases.
     */
    private void startSession() {
        infoIcon.setVisibility(View.GONE);
        mainText.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        currentStage = (currentStage == Stage.REST) ? Stage.LEFT_LEG : Stage.COUNTDOWN;

        if (currentStage == Stage.LEFT_LEG) {
            startBalanceTimer();
        } else {
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000);
                    timerText.setText(secondsLeft > 0 ? String.valueOf(secondsLeft) : "Go!");
                }
                public void onFinish() {
                    currentStage = Stage.RIGHT_LEG;
                    startBalanceTimer();
                }
            }.start();
        }
    }

    /**
     * Starts the balance timer for the current leg.
     */
    private void startBalanceTimer() {
        isBalancing = true;
        timerText.setText("10");
        balanceTimer = new CountDownTimer(BALANCE_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                isBalancing = false;
                if (currentStage == Stage.RIGHT_LEG) {
                    currentStage = Stage.REST;
                    showRestDialog("Great! Now take a break and press Start when you're ready for the left leg.");
                } else if (currentStage == Stage.LEFT_LEG) {
                    currentStage = Stage.DONE;
                    finishSuccess();
                }
            }
        }.start();
    }

    /**
     * Displays rest instructions between leg tasks.
     */
    private void showRestDialog(String message) {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle("Rest")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    startButton.setVisibility(View.VISIBLE);
                    mainText.setVisibility(View.VISIBLE);
                    infoIcon.setVisibility(View.VISIBLE);
                    mainText.setText("Press Start for the left leg when you are ready.");
                })
                .show();
    }

    /**
     * Handles task completion logic and saves to logbook.
     */
    private void finishSuccess() {
        timerText.setText("");
        saveLastDoneTime();
        saveTaskCompletion(System.currentTimeMillis());
        if (!isFinishing() && !isDestroyed()) {
            new AlertDialog.Builder(this)
                    .setTitle("🎉 Balance Success!")
                    .setMessage("Great! You completed the balance task successfully!")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent(BalanceTaskActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Saves the last done timestamp to preferences.
     */
    private void saveLastDoneTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_DONE, System.currentTimeMillis()).apply();
    }

    /**
     * Saves task completion entry to the database.
     */
    private void saveTaskCompletion(long timestamp) {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("Balance", timestamp));
        }).start();
    }

    /**
     * Checks if the balance task was completed today.
     */
    private boolean isTaskCompletedToday() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastDoneMillis = prefs.getLong(KEY_LAST_DONE, 0);
        if (lastDoneMillis == 0) return false;
        Calendar now = Calendar.getInstance();
        Calendar lastDone = Calendar.getInstance();
        lastDone.setTimeInMillis(lastDoneMillis);
        return isSameDay(now, lastDone) && now.get(Calendar.HOUR_OF_DAY) >= 7;
    }

    /**
     * Checks if two Calendar instances represent the same day.
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Disables the task button and informs the user.
     */
    private void blockTask() {
        startButton.setEnabled(false);
        startButton.setText("✅ Already done today");
        mainText.setText("You’ve already completed this today.\nCome back after 7 AM tomorrow.");
        Toast.makeText(this, "Task already done today.", Toast.LENGTH_LONG).show();
    }

    /**
     * Displays a dialog when the user fails the balance task.
     */
    private void showFailDialog() {
        isBalancing = false;
        if (balanceTimer != null) balanceTimer.cancel();
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Oops!")
                    .setMessage("You failed. Try again?")
                    .setPositiveButton("Try Again", (dialog, which) -> restartSession())
                    .setNegativeButton("NO :(", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    /**
     * Resets the UI for a new balance session.
     */
    private void restartSession() {
        currentStage = Stage.READY;
        mainText.setVisibility(View.VISIBLE);
        infoIcon.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        timerText.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            filteredX = ALPHA * x + (1 - ALPHA) * filteredX;
            filteredY = ALPHA * y + (1 - ALPHA) * filteredY;
            balanceView.updateBalance(filteredX, filteredY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Displays the instruction dialog.
     */
    private void showInstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage("Place your phone on your upward-facing palm, stretch your arm, and lift your right leg. Keep the ball inside the circle.")
                .setPositiveButton("OK", null)
                .show();
    }
}
