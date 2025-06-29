package com.example.healthpet.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

/**
 * HomeActivity is the main screen of the HealthPet app.
 * It displays various task buttons and the Koala pet whose mood depends on completed tasks.
 */
public class HomeActivity extends AppCompatActivity {

    private ScrollView rootScrollView;
    private TextView welcomeTextView;

    private Button stepGoalButton, waterGoalButton;
    private Button memoryTaskButton, balanceTaskButton, breathingTaskButton;
    private Button changeBackgroundButton, logbookButton;

    private int currentBackground = 1;
    private LottieAnimationView[] koalas;
    private int currentMood = 3;

    private Handler handler = new Handler();

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1;

    /**
     * Called when the activity is starting.
     * Initializes views, checks permissions and task completion status.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        DailyResetManager.checkAndResetIfNeeded(this);

        SharedPreferences prefs = getSharedPreferences("HealthPetPrefs", MODE_PRIVATE);

        boolean questionnaireDone = prefs.getBoolean("questionnaireDone", false);
        if (!questionnaireDone) {
            Intent intent = new Intent(this, QuestionnaireActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        rootScrollView = findViewById(R.id.rootScrollView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        stepGoalButton = findViewById(R.id.stepGoalButton);
        waterGoalButton = findViewById(R.id.waterGoalButton);
        memoryTaskButton = findViewById(R.id.memoryTaskButton);
        balanceTaskButton = findViewById(R.id.balanceTaskButton);
        breathingTaskButton = findViewById(R.id.breathingTaskButton);
        changeBackgroundButton = findViewById(R.id.change_background);
        logbookButton = findViewById(R.id.logbookButton);
        ImageButton infoButton = findViewById(R.id.infoButton);

        // Initialize Koala mood views
        koalas = new LottieAnimationView[]{
                findViewById(R.id.koala_1),
                findViewById(R.id.koala_2),
                findViewById(R.id.koala_3),
                findViewById(R.id.koala_4)
        };

        showKoala(currentMood);

        // Set button colors
        stepGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        waterGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        memoryTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        balanceTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        breathingTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        checkBreathingTaskBlocked(prefs);

        // Set button listeners
        stepGoalButton.setOnClickListener(v -> startActivity(new Intent(this, StepGoalActivity.class)));
        waterGoalButton.setOnClickListener(v -> startActivity(new Intent(this, WaterGoalActivity.class)));
        memoryTaskButton.setOnClickListener(v -> startActivity(new Intent(this, MemoryTaskActivity.class)));
        balanceTaskButton.setOnClickListener(v -> startActivity(new Intent(this, BalanceTaskActivity.class)));
        breathingTaskButton.setOnClickListener(v -> {
            boolean breathingBlocked = prefs.getBoolean("breathingBlocked", false);
            if (breathingBlocked) {
                Toast.makeText(this, "Breathing task is blocked due to respiratory issues.", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, BreathingTaskActivity.class));
            }
        });
        logbookButton.setOnClickListener(v -> startActivity(new Intent(this, LogbookActivity.class)));

        infoButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Info")
                    .setMessage("Complete tasks to improve your koala's mood! Mood resets every day at 7 AM.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        String userName = prefs.getString("userName", "User");
        welcomeTextView.setText("Welcome back " + userName + "!");

        checkActivityRecognitionPermission();

        checkTasksAndUpdateMood();

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

    /**
     * Updates the UI for the breathing task button depending on whether the task is blocked.
     *
     * @param prefs SharedPreferences containing breathingBlocked status
     */
    private void checkBreathingTaskBlocked(SharedPreferences prefs) {
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

    /**
     * Checks task completion status from the database and updates the Koala's mood accordingly.
     */
    private void checkTasksAndUpdateMood() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "task-database").build();

            long todayStartMillis = getTodayStartMillisAt7();

            boolean stepDone = db.taskDao().isTaskCompletedAfter("StepGoal", todayStartMillis);
            boolean waterDone = db.taskDao().isTaskCompletedAfter("Water", todayStartMillis);
            boolean breathingDone = db.taskDao().isTaskCompletedAfter("Breathing", todayStartMillis);
            boolean balanceDone = db.taskDao().isTaskCompletedAfter("Balance", todayStartMillis);
            boolean memoryDone = db.taskDao().isTaskCompletedAfter("Memory", todayStartMillis);

            int tasksCompleted = 0;
            if (stepDone) tasksCompleted++;
            if (waterDone) tasksCompleted++;
            if (breathingDone) tasksCompleted++;
            if (balanceDone) tasksCompleted++;
            if (memoryDone) tasksCompleted++;

            int mood;
            if (tasksCompleted >= 5) {
                mood = 0;
            } else if (tasksCompleted >= 3) {
                mood = 1;
            } else if (tasksCompleted == 2) {
                mood = 2;
            } else {
                mood = 3;
            }

            currentMood = mood;

            runOnUiThread(() -> {
                showKoala(currentMood);

                if (mood == 0) {
                    welcomeTextView.setText("🎉 All tasks done!");
                } else {
                    String userName = getSharedPreferences("HealthPetPrefs", MODE_PRIVATE)
                            .getString("userName", "User");
                    welcomeTextView.setText("Hi " + userName + "! Complete more tasks to make Koala happier!");
                }
            });
        }).start();
    }

    /**
     * Returns the timestamp for today at 7 AM, or yesterday at 7 AM if current time is before 7 AM.
     *
     * @return timestamp in milliseconds
     */
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

    /**
     * Displays the Koala animation corresponding to the given mood.
     *
     * @param mood Koala mood index (0 = happy, 1 = neutral, 2 = sad, 3 = crying)
     */
    private void showKoala(int mood) {
        for (int i = 0; i < koalas.length; i++) {
            koalas[i].setVisibility(i == mood ? View.VISIBLE : View.GONE);
            if (i == mood) koalas[i].playAnimation();
        }
    }

    /**
     * Checks and requests activity recognition permission if necessary.
     */
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

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode  The request code passed in requestPermissions
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
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

    /**
     * Starts the step counter foreground service.
     */
    private void startStepCounterService() {
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
