package com.example.healthpet;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BreathingTaskActivity extends AppCompatActivity {

    private TextView instructionText;
    private ProgressBar progressBar;

    private static final int BREATH_IN_DURATION = 4000;
    private static final int HOLD_DURATION = 7000;
    private static final int EXHALE_DURATION = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_task);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        instructionText = findViewById(R.id.instructionText);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setMax(EXHALE_DURATION);
        startExercise();
    }

    private void startExercise() {
        instructionText.setText("Be ready and follow instructions...");
        new CountDownTimer(1000, 1000) {
            public void onFinish() {
                startBreathingCycle();
            }

            public void onTick(long millisUntilFinished) { }
        }.start();
    }

    private void startBreathingCycle() {
        instructionText.setText("Breathe in...");
        animateProgress(BREATH_IN_DURATION, () -> {
            instructionText.setText("Hold...");
            animateProgress(HOLD_DURATION, () -> {
                instructionText.setText("Exhale...");
                animateProgress(EXHALE_DURATION, () -> {
                    instructionText.setText("Well done!");
                });
            });
        });
    }

    private void animateProgress(int duration, Runnable onComplete) {
        progressBar.setProgress(0);
        new CountDownTimer(duration, 50) {
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(duration - (int) millisUntilFinished);
            }

            public void onFinish() {
                progressBar.setProgress(duration);
                onComplete.run();
            }
        }.start();
    }
}
