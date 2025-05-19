package com.example.healthpet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;

public class HomeActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private ImageView petImageView;

    private Button stepGoalButton, waterGoalButton, restGoalButton;
    private Button memoryTaskButton, balanceTaskButton, breathingTaskButton;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeTextView = findViewById(R.id.welcomeTextView);



        stepGoalButton = findViewById(R.id.stepGoalButton);
        waterGoalButton = findViewById(R.id.waterGoalButton);
        restGoalButton = findViewById(R.id.restGoalButton);
        memoryTaskButton = findViewById(R.id.memoryTaskButton);
        balanceTaskButton = findViewById(R.id.balanceTaskButton);
        breathingTaskButton = findViewById(R.id.breathingTaskButton);

        // Set distinct background colors for buttons
        stepGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        waterGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        restGoalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        memoryTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        balanceTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        breathingTaskButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        stepGoalButton.setOnClickListener(v -> {
            // TODO: Start step goal activity
        });

        waterGoalButton.setOnClickListener(v -> {
            // TODO: Start water goal activity
        });

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

        Button showKoalaButton = findViewById(R.id.showKoalaButton);
        LottieAnimationView koalaView = findViewById(R.id.koala_eating);

        showKoalaButton.setOnClickListener(v -> {
            koalaView.setVisibility(View.VISIBLE);
        });
    }
}
