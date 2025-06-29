package com.example.healthpet.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.util.Calendar;

/**
 * BreathingTaskActivity handles the guided breathing exercise in the HealthPet app.
 * It animates the breathing cycle, tracks completion, and saves task results.
 */
public class BreathingTaskActivity extends AppCompatActivity {

    private static final int BREATH_IN_DURATION = 4000;
    private static final int HOLD_DURATION = 7000;
    private static final int EXHALE_DURATION = 8000;
    private static final int TOTAL_CYCLES = 4;

    private int currentCycle = 0;

    private TextView instructionText;
    private View breathingCircle;
    private Button startButton;
    private LottieAnimationView koalaView;

    private static final String PREFS_NAME = "BreathingPrefs";
    private static final String KEY_LAST_DONE = "lastBreathingDone";

    /**
     * Initializes the activity, views, and breathing task logic.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_task);

        koalaView = findViewById(R.id.Koala_Breathing);
        instructionText = findViewById(R.id.instructionText);
        breathingCircle = findViewById(R.id.breathingCircle);
        startButton = findViewById(R.id.startButton);

        breathingCircle.setScaleX(0.2f);
        breathingCircle.setScaleY(0.2f);

        koalaView.setVisibility(View.VISIBLE);
        koalaView.setAnimation("Koala_Breathing.json");
        koalaView.playAnimation();

        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            startButton.setOnClickListener(v -> {
                startButton.setVisibility(View.GONE);
                currentCycle = 0;
                startBreathingCycle();
            });
        }
    }

    /**
     * Starts one full breathing cycle including inhale, hold, and exhale phases.
     */
    private void startBreathingCycle() {
        if (currentCycle >= TOTAL_CYCLES) {
            instructionText.setText("🎉 Geschafft!");

            saveTaskCompletion();
            saveLastDoneTime();

            koalaView.setAnimation("Koala_HeartEyes.json");
            koalaView.playAnimation();

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Congratulations!")
                    .setMessage("You finished the breathing task. Well done!")
                    .setPositiveButton("OK", (dialog, which) -> {
                        currentCycle = 0;
                        breathingCircle.setScaleX(0.2f);
                        breathingCircle.setScaleY(0.2f);
                        instructionText.setText("Bereit für noch eine Runde?");
                        startButton.setText("Start");
                        startButton.setVisibility(View.VISIBLE);
                        blockTask();
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        currentCycle++;
        instructionText.setText("Einatmen...");

        animateBreathingPhase(1.5f, BREATH_IN_DURATION, () -> {
            instructionText.setText("Anhalten...");

            new Handler().postDelayed(() -> {
                instructionText.setText("Ausatmen...");
                animateBreathingPhase(0.2f, EXHALE_DURATION, this::startBreathingCycle);
            }, HOLD_DURATION);
        });
    }

    /**
     * Animates the breathing circle for the given phase.
     *
     * @param scale      Target scale of the circle.
     * @param duration   Animation duration in milliseconds.
     * @param onComplete Runnable to run after animation ends.
     */
    private void animateBreathingPhase(float scale, int duration, Runnable onComplete) {
        breathingCircle.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .withEndAction(onComplete)
                .start();
    }

    /**
     * Saves the timestamp of the last completed breathing task.
     */
    private void saveLastDoneTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_DONE, System.currentTimeMillis()).apply();
    }

    /**
     * Checks if the breathing task was already completed today.
     *
     * @return true if completed today after 7 AM, false otherwise.
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
            return false;
        }
    }

    /**
     * Compares two Calendar instances to see if they represent the same day.
     *
     * @param cal1 First calendar.
     * @param cal2 Second calendar.
     * @return true if both represent the same day.
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Disables the task button and informs the user that the task is already completed.
     */
    private void blockTask() {
        startButton.setEnabled(false);
        startButton.setText("✅ Already done today");
        instructionText.setText("You’ve already completed this today.\nCome back after 7 AM tomorrow.");
        Toast.makeText(this, "Task already done today.", Toast.LENGTH_LONG).show();
    }

    /**
     * Saves the breathing task completion to the local database logbook.
     */
    private void saveTaskCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("Breathing", System.currentTimeMillis()));
        }).start();
    }
}
