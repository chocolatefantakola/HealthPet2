package com.example.healthpet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import com.example.healthpet.R;
import com.example.healthpet.model.TaskCompletion;
import com.example.healthpet.data.AppDatabase;

public class BalanceTaskActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;


    private TextView timerText;
    private Button startButton;
    private BalanceView balanceView;
    private TextView mainText;
    private ImageView infoIcon;

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

        mainText = findViewById(R.id.mainText);
        infoIcon = findViewById(R.id.infoIcon);

        infoIcon.setOnClickListener(v -> showInstructionDialog());


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Balance Task");
        }


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
        infoIcon.setVisibility(View.GONE);
        mainText.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        currentStage = (currentStage == Stage.REST) ? Stage.LEFT_LEG : Stage.COUNTDOWN;

        if (currentStage == Stage.LEFT_LEG) {
            startBalanceTimer();
        } else {
            // Countdown für das rechte Bein
            new CountDownTimer(4000, 1000) {  // 3,2,1,Go!
                public void onTick(long millisUntilFinished) {
                    int secondsLeft = (int) (millisUntilFinished / 1000);
                    if (secondsLeft > 0) {
                        timerText.setText(String.valueOf(secondsLeft));
                    } else {
                        timerText.setText("Go!");
                    }
                }
                public void onFinish() {
                    currentStage = Stage.RIGHT_LEG;
                    startBalanceTimer();
                }
            }.start();
        }
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
                    // Jetzt Pause – warte bis der User wieder startet
                    showRestDialog("Great! Now take a break and press Start when you're ready for the left leg.");
                } else if (currentStage == Stage.LEFT_LEG) {
                    currentStage = Stage.DONE;
                    finishSuccess();
                }
            }
        }.start();
    }

    private void showRestDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Rest")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    startButton.setVisibility(View.VISIBLE);
                    mainText.setVisibility(View.VISIBLE);
                    infoIcon.setVisibility(View.VISIBLE);
                    mainText.setText("Press Start for the left leg when you are ready.");
                })
                .show();
    }




    private void startRest() {
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                currentStage = Stage.LEFT_LEG;
                // Zeig den Dialog zur Info, wenn du magst
                showInstructionDialogForLeftLeg();

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

    private void showInstructionDialogForLeftLeg() {
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage("Place your phone on your upward-facing palm, stretch your arm, and lift your left leg. Keep the ball inside the circle.")
                .setPositiveButton("OK", null)
                .show();
    }



    private void finishSuccess() {
        timerText.setText("");

        long now = System.currentTimeMillis();
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task-database").build();
            db.taskDao().insert(new TaskCompletion("Balance", now));
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
                    .setNegativeButton("NO :(", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    private void restartSession() {
        currentStage = Stage.READY;
        mainText.setVisibility(View.VISIBLE);
        infoIcon.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        timerText.setText("");
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


    private void showInstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage("Place your phone on your upward-facing palm, stretch your arm, and lift your right leg. Keep the ball inside the circle.")
                .setPositiveButton("OK", null)
                .show();
    }


}
