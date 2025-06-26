package com.example.healthpet.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.healthpet.R;

public class BreathingTaskActivity extends AppCompatActivity {

    private TextView instructionText;
    private ProgressBar progressBar;
    private Button startButton;

    private LinearLayout choiceLayout;
    private Button btn478, btnAlternate;
    private ImageView info478, infoAlternate;

    private static final int BREATH_IN_DURATION = 4000;
    private static final int HOLD_DURATION = 7000;
    private static final int EXHALE_DURATION = 8000;
    private static final int TOTAL_CYCLES = 4;

    private int currentCycle = 0;
    private enum BreathingMode { NONE, FOUR_SEVEN_EIGHT, ALTERNATE_NOSTRIL }
    private BreathingMode currentMode = BreathingMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_task);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        instructionText = findViewById(R.id.instructionText);
        progressBar = findViewById(R.id.progressBar);
        startButton = findViewById(R.id.startButton);

        choiceLayout = findViewById(R.id.choiceLayout);
        btn478 = findViewById(R.id.btn478);
        btnAlternate = findViewById(R.id.btnAlternate);
        info478 = findViewById(R.id.info478);
        infoAlternate = findViewById(R.id.infoAlternate);

        progressBar.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);

        btn478.setOnClickListener(v -> {
            currentMode = BreathingMode.FOUR_SEVEN_EIGHT;
            choiceLayout.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            instructionText.setText("4-7-8 Breathing selected.\nPress START to begin.");
        });

        btnAlternate.setOnClickListener(v -> {
            currentMode = BreathingMode.ALTERNATE_NOSTRIL;
            choiceLayout.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            instructionText.setText("Alternate Nostril Breathing selected.\nPress START to begin.");
        });

        info478.setOnClickListener(v -> showInfoDialog("4-7-8 Breathing", "Developed by Dr. Andrew Weil, this technique calms your nervous system.\n\nHow to do it:\n- Inhale through your nose for 4 counts\n- Hold your breath for 7 counts\n- Exhale through your mouth for 8 counts (whooshing sound)\n- Repeat up to 4 times."));

        infoAlternate.setOnClickListener(v -> showInfoDialog("Alternate Nostril Breathing", "Often used in yoga, this technique may improve focus and lower blood pressure.\n\nHow to do it:\n- Close right nostril with thumb, inhale left\n- Close left nostril with finger, exhale right\n- Inhale right\n- Close right, exhale left\n- Repeat for several minutes."));

        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            currentCycle = 0;
            startBreathingCycle();
        });
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

    private void startBreathingCycle() {
        if (currentCycle >= TOTAL_CYCLES) {
            instructionText.setText("🎉 Done! You finished all cycles.");
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
            startButton.setText("RESTART");
            startButton.setVisibility(View.VISIBLE);
            return;
        }

        currentCycle++;
        if (currentMode == BreathingMode.FOUR_SEVEN_EIGHT) {
            instructionText.setText("Cycle " + currentCycle + "/" + TOTAL_CYCLES + "\nBreathe in...");
            animateProgress(BREATH_IN_DURATION, () -> {
                instructionText.setText("Hold...");
                animateProgress(HOLD_DURATION, () -> {
                    instructionText.setText("Exhale...");
                    animateProgress(EXHALE_DURATION, this::startBreathingCycle);
                });
            });
        } else if (currentMode == BreathingMode.ALTERNATE_NOSTRIL) {
            instructionText.setText("Cycle " + currentCycle + "/" + TOTAL_CYCLES + "\nInhale left - Exhale right");
            animateProgress(4000, () -> {
                instructionText.setText("Inhale right - Exhale left");
                animateProgress(4000, this::startBreathingCycle);
            });
        }
    }

    private void animateProgress(int duration, Runnable onComplete) {
        progressBar.setMax(duration);
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
