package com.example.healthpet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ScrollView roootScrollView;
    private TextView welcomeTextView;
    private ImageView petImageView;

    private Button stepGoalButton, waterGoalButton, restGoalButton;
    private Button memoryTaskButton, balanceTaskButton, breathingTaskButton;
    private Button makeHappyButton;

    private Button changeBackgroundButton;
    private int currentBackground = 1;

    private LottieAnimationView[] koalas;
    private int currentLevel = 3;
    private Handler handler = new Handler();
    private Runnable sadnessRunnable;

    private static final String PREFS_NAME = "WaterPrefs";
    private static final String KEY_LAST_WATER_TIME = "lastWaterTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        ScrollView rootScrollView = findViewById(R.id.rootScrollView);
        stepGoalButton = findViewById(R.id.stepGoalButton);
        waterGoalButton = findViewById(R.id.waterGoalButton);
        restGoalButton = findViewById(R.id.restGoalButton);
        memoryTaskButton = findViewById(R.id.memoryTaskButton);
        balanceTaskButton = findViewById(R.id.balanceTaskButton);
        breathingTaskButton = findViewById(R.id.breathingTaskButton);
        makeHappyButton = findViewById(R.id.makeHappyButton);
        changeBackgroundButton = findViewById(R.id.change_background);

        stepGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        waterGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        restGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        memoryTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        balanceTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        breathingTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        stepGoalButton.setOnClickListener(v -> {
            // TODO: Start step goal activity
        });

        waterGoalButton.setOnClickListener(v -> handleWaterGoalClick());

        restGoalButton.setOnClickListener(v -> {
            // TODO: Start rest goal activity
        });

        memoryTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MemoryTaskActivity.class);
            startActivity(intent);
        });

        balanceTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BalanceTaskActivity.class);
            startActivity(intent);
        });

        breathingTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BreathingTaskActivity.class);
            startActivity(intent);
        });

        koalas = new LottieAnimationView[]{
                findViewById(R.id.koala_1),
                findViewById(R.id.koala_2),
                findViewById(R.id.koala_3),
                findViewById(R.id.koala_4),
                findViewById(R.id.koala_5)
        };

        showKoala(currentLevel);
        scheduleSadness();

        makeHappyButton.setOnClickListener(v -> {
            handler.removeCallbacks(sadnessRunnable);
            if (currentLevel > 0) {
                currentLevel--;
                showKoala(currentLevel);
            }
            scheduleSadness();
        });

        changeBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentBackground) {
                    case 0:
                        rootScrollView.setBackgroundResource(R.drawable.bg_gradient); // pink
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
            }
        });
    }

    private void handleWaterGoalClick() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastCompleted = prefs.getLong(KEY_LAST_WATER_TIME, 0);
        long now = System.currentTimeMillis();

        if (!isWaterTaskAvailable(lastCompleted, now)) {
            long millisLeft = getMillisUntilNext7AM(lastCompleted);
            long hours = millisLeft / (1000 * 60 * 60);
            long minutes = (millisLeft / (1000 * 60)) % 60;

            new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                    .setTitle("🚫 Task Locked")
                    .setMessage("Water task will be available in " + hours + "h " + minutes + "m.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                    .setTitle("💧 Daily Water Check")
                    .setMessage("Have you really drunk all your water today?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        prefs.edit().putLong(KEY_LAST_WATER_TIME, now).apply();
                        new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                                .setTitle("🎉 Hydration Success!")
                                .setMessage("Great! You’ve reached your daily water goal. Stay hydrated and keep it up!")
                                .setPositiveButton("OK", null)
                                .show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private boolean isWaterTaskAvailable(long lastTime, long now) {
        return now >= getNext7AM(lastTime);
    }

    private long getNext7AM(long fromTimeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromTimeMillis);
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getMillisUntilNext7AM(long fromTimeMillis) {
        return getNext7AM(fromTimeMillis) - System.currentTimeMillis();
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
        handler.postDelayed(sadnessRunnable, 5000);
    }
}
