package com.example.healthpet;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ScrollView rootScrollView;
    private TextView welcomeTextView;

    private Button stepGoalButton, waterGoalButton, restGoalButton;
    private Button memoryTaskButton, balanceTaskButton, breathingTaskButton;
    private Button makeHappyButton, changeBackgroundButton, logbookButton;

    private int currentBackground = 1;
    private LottieAnimationView[] koalas;
    private int currentLevel = 3;
    private Handler handler = new Handler();
    private Runnable sadnessRunnable;

    private static final String PREFS_NAME = "WaterPrefs";
    private static final String KEY_LAST_WATER_TIME = "lastWaterTime";

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Views
        rootScrollView = findViewById(R.id.rootScrollView);
        welcomeTextView = findViewById(R.id.welcomeTextView);

        stepGoalButton = findViewById(R.id.stepGoalButton);
        waterGoalButton = findViewById(R.id.waterGoalButton);
        restGoalButton = findViewById(R.id.restGoalButton);
        memoryTaskButton = findViewById(R.id.memoryTaskButton);
        balanceTaskButton = findViewById(R.id.balanceTaskButton);
        breathingTaskButton = findViewById(R.id.breathingTaskButton);
        makeHappyButton = findViewById(R.id.makeHappyButton);
        changeBackgroundButton = findViewById(R.id.change_background);
        logbookButton = findViewById(R.id.logbookButton);

        // Button Farben
        stepGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        waterGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        restGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        memoryTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        balanceTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        breathingTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Navigation
        stepGoalButton.setOnClickListener(v -> startActivity(new Intent(this, StepGoalActivity.class)));
        logbookButton.setOnClickListener(v -> startActivity(new Intent(this, LogbookActivity.class)));
        memoryTaskButton.setOnClickListener(v -> startActivity(new Intent(this, MemoryTaskActivity.class)));
        balanceTaskButton.setOnClickListener(v -> startActivity(new Intent(this, BalanceTaskActivity.class)));
        breathingTaskButton.setOnClickListener(v -> startActivity(new Intent(this, BreathingTaskActivity.class)));

        checkActivityRecognitionPermission();


        // Water Goal
        waterGoalButton.setOnClickListener(v -> handleWaterGoalClick());

        // Koalas
        koalas = new LottieAnimationView[]{
                findViewById(R.id.koala_1), findViewById(R.id.koala_2), findViewById(R.id.koala_3),
                findViewById(R.id.koala_4), findViewById(R.id.koala_5)
        };
        showKoala(currentLevel);
        scheduleSadness();

        makeHappyButton.setOnClickListener(v -> {
            handler.removeCallbacks(sadnessRunnable);
            if (currentLevel > 0) currentLevel--;
            showKoala(currentLevel);
            scheduleSadness();
        });

        changeBackgroundButton.setOnClickListener(v -> {
            switch (currentBackground) {
                case 0:
                    rootScrollView.setBackgroundResource(R.drawable.bg_gradient);
                    currentBackground = 1;
                    break;
                case 1:
                    rootScrollView.setBackgroundResource(R.drawable.bg_gradient_green);
                    currentBackground = 2;
                    break;
                case 2:
                    rootScrollView.setBackgroundResource(R.drawable.bg_gradient_red);
                    currentBackground = 0;
                    break;
            }
        });
    }

    private void handleWaterGoalClick() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastCompleted = prefs.getLong(KEY_LAST_WATER_TIME, 0);
        long now = System.currentTimeMillis();

        if (now < getNext7AM(lastCompleted)) {
            long millisLeft = getNext7AM(lastCompleted) - now;
            long hours = millisLeft / (1000 * 60 * 60);
            long minutes = (millisLeft / (1000 * 60)) % 60;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🚫 Task Locked")
                    .setMessage("Water task will be available in " + hours + "h " + minutes + "m.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("💧 Daily Water Check")
                    .setMessage("Have you really drunk all your water today?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        prefs.edit().putLong(KEY_LAST_WATER_TIME, now).apply();
                        if (currentLevel > 0) currentLevel--;
                        showKoala(currentLevel);

                        new Thread(() -> {
                            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                                    AppDatabase.class, "task-database").build();
                            db.taskDao().insert(new TaskCompletion("waterGoal", now));
                        }).start();

                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("🎉 Hydration Success!")
                                .setMessage("Great! You’ve reached your daily water goal. Stay hydrated and keep it up!")
                                .setPositiveButton("OK", null)
                                .show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private long getNext7AM(long fromTimeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromTimeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void showKoala(int level) {
        for (int i = 0; i < koalas.length; i++) {
            koalas[i].setVisibility(i == level ? View.VISIBLE : View.GONE);
            if (i == level) koalas[i].playAnimation();
        }
    }

    private void scheduleSadness() {
        sadnessRunnable = () -> {
            if (currentLevel < koalas.length - 1) {
                currentLevel++;
                showKoala(currentLevel);
                scheduleSadness();
            }
        };
        handler.postDelayed(sadnessRunnable, 20000);
    }
    private void checkActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_ACTIVITY_RECOGNITION);
            } else {
                startStepCounterService();
            }
        } else {
            startStepCounterService();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounterService();
            } else {
                Toast.makeText(this, "Permission denied: Step counter will not work properly.", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void startStepCounterService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }



}
