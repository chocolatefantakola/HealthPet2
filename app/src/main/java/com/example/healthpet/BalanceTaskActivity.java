package com.example.healthpet;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import java.util.Calendar;

public class BalanceTaskActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private TextView instructionText;
    private TextView timerText;
    private Button startButton;
    private BalanceView balanceView;

    private enum Stage {
        READY, COUNTDOWN, RIGHT_LEG, REST, LEFT_LEG, DONE
    }

    private Stage currentStage = Stage.READY;
    private boolean isBalancing = false;
    private static final int BALANCE_DURATION = 10000; // 10 Sekunden
    private CountDownTimer balanceTimer;

    private float filteredX = 0;
    private float filteredY = 0;
    private static final float ALPHA = 0.3f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_task);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Balance Task");
        }

        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);
        startButton = findViewById(R.id.startButton);
        balanceView = findViewById(R.id.balanceView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometerSensor == null) {
            Toast.makeText(this, "No accelerometer available!", Toast.LENGTH_SHORT).show();
        }

        balanceView.setBalanceFailListener(() -> {
            if (isBalancing) {
                runOnUiThread(this::showFailDialog);
            }
        });

        startButton.setOnClickListener(v -> startSession());
    }

    private void startSession() {
        currentStage = Stage.COUNTDOWN;
        startButton.setVisibility(View.GONE);
        instructionText.setText("Place your phone on your hand, stretch your arm and lift your right leg. Hold the ball inside the circle.");
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Starting in: " + (millisUntilFinished / 1000));
            }
            public void onFinish() {
                currentStage = Stage.RIGHT_LEG;
                startBalanceTimer();
            }
        }.start();
    }

    private void startBalanceTimer() {
        isBalancing = true;
        timerText.setText("10");
        balanceTimer = new CountDownTimer(BALANCE_DURATION, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                isBalancing = false;
                if (currentStage == Stage.RIGHT_LEG) {
                    currentStage = Stage.REST;
                    startRest();
                } else if (currentStage == Stage.LEFT_LEG) {
                    currentStage = Stage.DONE;
                    finishSuccess();
                }
            }
        }.start();
    }

    private void startRest() {
        instructionText.setText("Rest");
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                currentStage = Stage.LEFT_LEG;
                instructionText.setText("Place your phone on your hand, stretch your arm and lift your left leg. Hold the ball inside the circle.");
                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText("Starting in: " + (millisUntilFinished / 1000));
                    }
                    public void onFinish() {
                        startBalanceTimer();
                    }
                }.start();
            }
        }.start();
    }

    private void finishSuccess() {
        instructionText.setText("Well done!");
        timerText.setText("");

        long now = System.currentTimeMillis();
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task-database").build();
            db.taskDao().insert(new TaskCompletion("balanceTask", now));
        }).start();

        new AlertDialog.Builder(this)
                .setTitle("🎉 Balance Success!")
                .setMessage("Great! You completed the balance task successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(BalanceTaskActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showFailDialog() {
        isBalancing = false;
        if (balanceTimer != null) balanceTimer.cancel();

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Oops!")
                    .setMessage("You failed. Try again?")
                    .setPositiveButton("Try Again", (dialog, which) -> restartSession())
                    .setNegativeButton("Ok :(", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    private void restartSession() {
        currentStage = Stage.READY;
        instructionText.setText("Place your phone on your hand, stretch your arm and lift your right leg. Hold the ball inside the circle.");
        startButton.setVisibility(View.VISIBLE);
        timerText.setText("Press start to begin");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            filteredX = ALPHA * x + (1 - ALPHA) * filteredX;
            filteredY = ALPHA * y + (1 - ALPHA) * filteredY;

            balanceView.updateBalance(filteredX, filteredY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
