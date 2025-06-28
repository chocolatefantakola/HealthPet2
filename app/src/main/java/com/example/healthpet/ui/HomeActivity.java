package com.example.healthpet.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.service.StepCounterService;
import com.example.healthpet.util.DailyResetManager;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ScrollView rootScrollView;
    private TextView welcomeTextView;

    private Button stepGoalButton, waterGoalButton;
    private Button memoryTaskButton, balanceTaskButton, breathingTaskButton;
    private Button changeBackgroundButton, logbookButton;

    private int currentBackground = 1;
    private LottieAnimationView[] koalas;
    private int currentLevel = 3;
    private Handler handler = new Handler();
    private Runnable sadnessRunnable;

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        DailyResetManager.checkAndResetIfNeeded(this);

        // Views
        rootScrollView = findViewById(R.id.rootScrollView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        stepGoalButton = findViewById(R.id.stepGoalButton);
        waterGoalButton = findViewById(R.id.waterGoalButton);
        memoryTaskButton = findViewById(R.id.memoryTaskButton);
        balanceTaskButton = findViewById(R.id.balanceTaskButton);
        breathingTaskButton = findViewById(R.id.breathingTaskButton);
        changeBackgroundButton = findViewById(R.id.change_background);
        logbookButton = findViewById(R.id.logbookButton);

        // Button Colors
        stepGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        waterGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        memoryTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        balanceTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        breathingTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Check if breathing task is blocked
        checkBreathingTaskBlocked();

        // Navigation Buttons
        stepGoalButton.setOnClickListener(v -> startActivity(new Intent(this, StepGoalActivity.class)));
        logbookButton.setOnClickListener(v -> startActivity(new Intent(this, LogbookActivity.class)));
        memoryTaskButton.setOnClickListener(v -> startActivity(new Intent(this, MemoryTaskActivity.class)));
        balanceTaskButton.setOnClickListener(v -> startActivity(new Intent(this, BalanceTaskActivity.class)));
        waterGoalButton.setOnClickListener(v -> startActivity(new Intent(this, WaterGoalActivity.class)));

        breathingTaskButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("HealthPetPrefs", MODE_PRIVATE);
            boolean breathingBlocked = prefs.getBoolean("breathingBlocked", false);
            if (breathingBlocked) {
                Toast.makeText(this, "Breathing task is blocked due to respiratory issues.", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BreathingTaskActivity.class));
            }
        });

        checkActivityRecognitionPermission();

        // Koalas Setup
        koalas = new LottieAnimationView[]{
                findViewById(R.id.koala_1), findViewById(R.id.koala_2), findViewById(R.id.koala_3),
                findViewById(R.id.koala_4), findViewById(R.id.koala_5)
        };
        showKoala(currentLevel);

        checkAllTasksCompletion();

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

    // 🔥 Check if breathing is blocked and update button UI
    private void checkBreathingTaskBlocked() {
        SharedPreferences prefs = getSharedPreferences("HealthPetPrefs", MODE_PRIVATE);
        boolean breathingBlocked = prefs.getBoolean("breathingBlocked", false);

        if (breathingBlocked) {
            breathingTaskButton.setEnabled(false);
            breathingTaskButton.setAlpha(0.5f);
            breathingTaskButton.setText("Breathing Task Blocked 🚫");
        } else {
            breathingTaskButton.setEnabled(true);
            breathingTaskButton.setAlpha(1.0f);
            breathingTaskButton.setText("Breathing Task");
        }
    }

    private void checkAllTasksCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "task-database").build();

            long todayStartMillis = getTodayStartMillisAt7();

            boolean stepDone = db.taskDao().isTaskCompletedAfter("StepGoal", todayStartMillis);
            boolean waterDone = db.taskDao().isTaskCompletedAfter("waterGoal", todayStartMillis);
            boolean breathingDone = db.taskDao().isTaskCompletedAfter("Breathing", todayStartMillis);
            boolean balanceDone = db.taskDao().isTaskCompletedAfter("Balance", todayStartMillis);
            boolean memoryDone = db.taskDao().isTaskCompletedAfter("Memory", todayStartMillis);

            boolean allDone = stepDone && waterDone && breathingDone && balanceDone && memoryDone;

            runOnUiThread(() -> {
                if (allDone) {
                    currentLevel = 0;
                    showKoala(currentLevel);
                    handler.removeCallbacks(sadnessRunnable);
                    welcomeTextView.setText("🎉 All tasks done! Koala is super happy!");
                } else {
                    scheduleSadness();
                }
            });
        }).start();
    }

    private long getTodayStartMillisAt7() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (System.currentTimeMillis() < calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return calendar.getTimeInMillis();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ACTIVITY_RECOGNITION);
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
