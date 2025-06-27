package com.example.healthpet.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

public class WaterGoalActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterPrefs";
    private static final String KEY_WATER_AMOUNT = "waterAmount";

    private long waterAmount = 0;
    private TextView textWaterAmount;

    private boolean goalReached = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_goal);

        textWaterAmount = findViewById(R.id.text_water_amount);
        Button buttonQuarter = findViewById(R.id.button_quarter);
        Button buttonHalf = findViewById(R.id.button_half);
        Button buttonOne = findViewById(R.id.button_one);
        Button buttonDone = findViewById(R.id.button_done);

        // Lade gespeichertes Wasser
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        waterAmount = prefs.getLong(KEY_WATER_AMOUNT, 0);
        goalReached = prefs.getBoolean("goalReached", false);


        updateWaterText();

        buttonQuarter.setOnClickListener(v -> addWater(250));
        buttonHalf.setOnClickListener(v -> addWater(500));
        buttonOne.setOnClickListener(v -> addWater(1000));
        buttonDone.setOnClickListener(v -> finishWaterGoal());
    }

    private void addWater(long ml) {
        waterAmount += ml;
        saveWaterAmount(waterAmount);
        updateWaterText();

        if (!goalReached && waterAmount >= 2000) {
            goalReached = true;
            saveGoalReachedFlag();
            showSuccess();
        }
    }


    private void updateWaterText() {
        textWaterAmount.setText(waterAmount + " ml");
    }

    private void saveWaterAmount(long amount) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_WATER_AMOUNT, amount).apply();
    }

    private void showSuccess() {
        saveTaskCompletion();

        new AlertDialog.Builder(this)
                .setTitle("🎉 Hydration Goal Reached!")
                .setMessage("You’ve reached your daily goal of 2 liters! Great job!")
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    private void finishWaterGoal() {
        new AlertDialog.Builder(this)
                .setTitle("💧 Progress Saved")
                .setMessage("You’ve logged " + waterAmount + " ml so far today.")
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    private void saveTaskCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("waterGoal", System.currentTimeMillis()));
        }).start();
    }

    private void saveGoalReachedFlag() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("goalReached", true).apply();
    }

}
