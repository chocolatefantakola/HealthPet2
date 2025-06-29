package com.example.healthpet.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.util.Calendar;

/**
 * WaterGoalActivity tracks the user's water intake and manages progress towards the daily goal.
 * It saves progress and handles completion logic.
 */
public class WaterGoalActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterPrefs";
    private static final String KEY_WATER_AMOUNT = "waterAmount";
    private static final String KEY_LAST_DONE = "lastWaterDone";

    private long waterAmount = 0;
    private boolean goalReached = false;
    private TextView textWaterAmount;

    private Button buttonQuarter, buttonHalf, buttonOne, buttonDone;

    /**
     * Initializes the activity, loads saved data, and sets up buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_goal);

        textWaterAmount = findViewById(R.id.text_water_amount);
        buttonQuarter = findViewById(R.id.button_quarter);
        buttonHalf = findViewById(R.id.button_half);
        buttonOne = findViewById(R.id.button_one);
        buttonDone = findViewById(R.id.button_done);

        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            loadSavedData();
            setupButtons();
        }
    }

    /**
     * Sets up button click listeners for water intake.
     */
    private void setupButtons() {
        buttonQuarter.setOnClickListener(v -> addWater(250));
        buttonHalf.setOnClickListener(v -> addWater(500));
        buttonOne.setOnClickListener(v -> addWater(1000));
        buttonDone.setOnClickListener(v -> finishWaterGoal());
    }

    /**
     * Adds water intake and checks if goal is reached.
     *
     * @param ml Milliliters added.
     */
    private void addWater(long ml) {
        waterAmount += ml;
        saveWaterAmount(waterAmount);
        updateWaterText();

        if (!goalReached && waterAmount >= 2000) {
            goalReached = true;
            saveTaskCompletion();
            saveLastDoneTime();
            showSuccess();
            blockTask();
        }
    }

    /**
     * Updates the water amount text view.
     */
    private void updateWaterText() {
        textWaterAmount.setText(waterAmount + " ml");
    }

    /**
     * Loads saved water amount and goal status.
     */
    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        waterAmount = prefs.getLong(KEY_WATER_AMOUNT, 0);
        goalReached = prefs.getBoolean("goalReached", false);
        updateWaterText();
    }

    /**
     * Saves the current water amount.
     */
    private void saveWaterAmount(long amount) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_WATER_AMOUNT, amount).apply();
    }

    /**
     * Saves the goal reached flag.
     */
    private void saveGoalReachedFlag(boolean reached) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("goalReached", reached).apply();
    }

    /**
     * Saves the last done timestamp.
     */
    private void saveLastDoneTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_DONE, System.currentTimeMillis()).apply();
    }

    /**
     * Checks if task was completed today.
     */
    private boolean isTaskCompletedToday() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastDoneMillis = prefs.getLong(KEY_LAST_DONE, 0);
        if (lastDoneMillis == 0) return false;

        Calendar now = Calendar.getInstance();
        Calendar lastDone = Calendar.getInstance();
        lastDone.setTimeInMillis(lastDoneMillis);

        if (isSameDay(now, lastDone)) {
            return now.get(Calendar.HOUR_OF_DAY) >= 7;
        } else {
            resetDailyProgress();
            return false;
        }
    }

    /**
     * Checks if two Calendar instances represent the same day.
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Resets daily water intake progress.
     */
    private void resetDailyProgress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_WATER_AMOUNT, 0)
                .putBoolean("goalReached", false)
                .apply();
        waterAmount = 0;
        goalReached = false;
    }

    /**
     * Disables buttons and informs the user that task is done.
     */
    private void blockTask() {
        buttonQuarter.setEnabled(false);
        buttonHalf.setEnabled(false);
        buttonOne.setEnabled(false);
        buttonDone.setEnabled(false);
        textWaterAmount.setText("✅ Already completed today!");
        Toast.makeText(this, "You’ve already completed this today. Come back tomorrow after 7 AM.", Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a success dialog when the goal is reached.
     */
    private void showSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Hydration Goal Reached!")
                .setMessage("You’ve reached your daily goal of 2 liters! Great job!")
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    /**
     * Shows a dialog when the user finishes manually.
     */
    private void finishWaterGoal() {
        new AlertDialog.Builder(this)
                .setTitle("💧 Progress Saved")
                .setMessage("You’ve logged " + waterAmount + " ml so far today.")
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    /**
     * Saves task completion to the database.
     */
    private void saveTaskCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("Water", System.currentTimeMillis()));
        }).start();
    }
}
