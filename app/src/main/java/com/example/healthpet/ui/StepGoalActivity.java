package com.example.healthpet.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;
import com.example.healthpet.receiver.ResetReceiver;

import java.util.Calendar;

public class StepGoalActivity extends AppCompatActivity {

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;

    private final int dailyStepGoal = 10000;
    private boolean goalReached = false;

    private static final String PREFS_NAME = "stepPrefs";
    private static final String KEY_LAST_DONE = "lastStepDone";
    private static final String KEY_STEPS_TODAY = "stepsToday";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        stepsProgressBar.setMax(dailyStepGoal);

        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            checkIfGoalReached();
        }

        scheduleDailyReset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            loadSteps();
            checkIfGoalReached();
        }
    }

    private void loadSteps() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int stepsToday = prefs.getInt(KEY_STEPS_TODAY, 0);

        stepsTextView.setText("Steps done: " + stepsToday);
        stepsRemainingTextView.setText("Steps remaining: " + Math.max(dailyStepGoal - stepsToday, 0));
        stepsProgressBar.setProgress(stepsToday);
    }

    private void checkIfGoalReached() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int stepsToday = prefs.getInt(KEY_STEPS_TODAY, 0);

        if (stepsToday >= dailyStepGoal) {
            goalReached = true;
            saveTaskCompletion();
            saveLastDoneTime();
            Toast.makeText(this, "🎉 Step goal completed for today!", Toast.LENGTH_LONG).show();
            blockTask();
        }
    }

    private void blockTask() {
        stepsTextView.setText("✅ Step goal completed today!");
        stepsRemainingTextView.setText("Come back after 7 AM tomorrow.");
        stepsProgressBar.setProgress(dailyStepGoal);
        Toast.makeText(this, "You've already completed this task today.", Toast.LENGTH_LONG).show();
    }

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

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void saveLastDoneTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_DONE, System.currentTimeMillis()).apply();
    }

    private void saveTaskCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("StepGoal", System.currentTimeMillis()));
        }).start();
    }

    private void scheduleDailyReset() {
        Intent intent = new Intent(this, ResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
