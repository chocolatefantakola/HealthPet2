package com.example.healthpet.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

public class BreathingTaskActivity extends AppCompatActivity {

    private static final int BREATH_IN_DURATION = 4000; // 4 Sekunden
    private static final int HOLD_DURATION = 7000;       // 7 Sekunden
    private static final int EXHALE_DURATION = 8000;     // 8 Sekunden
    private static final int TOTAL_CYCLES = 4;

    private int currentCycle = 0;

    private TextView instructionText;
    private View breathingCircle;
    private Button startButton;

    private LottieAnimationView koalaView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_task);

        koalaView = findViewById(R.id.Koala_Breathing);
        koalaView.setVisibility(View.VISIBLE);
        koalaView.setAnimation("Koala_Breathing.json");
        koalaView.playAnimation();

        instructionText = findViewById(R.id.instructionText);
        breathingCircle = findViewById(R.id.breathingCircle);
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.GONE);
            currentCycle = 0;
            startBreathingCycle();
        });
        breathingCircle.setScaleX(0.2f);
        breathingCircle.setScaleY(0.2f);

    }

    private void startBreathingCycle() {
        if (currentCycle >= TOTAL_CYCLES) {
            instructionText.setText("🎉 Geschafft!");

            new Thread(() -> {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "task-database").fallbackToDestructiveMigration().build();
                db.taskDao().insert(new TaskCompletion("Breathing", System.currentTimeMillis()));
            }).start();

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
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        currentCycle++;
        instructionText.setText("Einatmen...");

        // Einatmen: Kreis wächst von klein zu groß
        animateBreathingPhase(1.5f, BREATH_IN_DURATION, () -> {
            instructionText.setText("Anhalten...");

            // Halten: 7s nichts tun, Kreis bleibt groß
            new Handler().postDelayed(() -> {
                instructionText.setText("Ausatmen...");

                // Ausatmen: Kreis wird wieder klein
                animateBreathingPhase(0.2f, EXHALE_DURATION, this::startBreathingCycle);
            }, HOLD_DURATION);
        });
    }


    private void animateBreathingPhase(float scale, int duration, Runnable onComplete) {
        breathingCircle.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .withEndAction(onComplete)
                .start();
    }
}
