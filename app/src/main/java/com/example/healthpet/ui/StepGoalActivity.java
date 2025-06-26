package com.example.healthpet.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthpet.R;
import com.example.healthpet.receiver.ResetReceiver;

import java.util.Calendar;

public class StepGoalActivity extends AppCompatActivity {

    private TextView stepsTextView, stepsRemainingTextView;
    private ProgressBar stepsProgressBar;

    private int dailyStepGoal = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        stepsTextView = findViewById(R.id.stepsTextView);
        stepsRemainingTextView = findViewById(R.id.stepsRemainingTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);

        stepsProgressBar.setMax(dailyStepGoal);

        scheduleDailyReset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSteps();
    }

    private void loadSteps() {
        SharedPreferences prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE);
        int stepsToday = prefs.getInt("stepsToday", 0);

        stepsTextView.setText("Steps done: " + stepsToday);
        stepsRemainingTextView.setText("Steps remaining: " + (dailyStepGoal - stepsToday));
        stepsProgressBar.setProgress(stepsToday);
    }
    private void scheduleDailyReset() {
        Intent intent = new Intent(this, ResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
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
