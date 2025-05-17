package com.example.healthpet;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

public class BalanceTaskActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private TextView instructionText;
    private TextView timerText;
    private TextView gyroX, gyroY, gyroZ;

    private enum Stage {
        READY, LEFT_LEG, REST, RIGHT_LEG, DONE
    }

    private Stage currentStage = Stage.READY;

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

        gyroX = findViewById(R.id.gyroX);
        gyroY = findViewById(R.id.gyroY);
        gyroZ = findViewById(R.id.gyroZ);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        startStageTimer();
    }

    private void startStageTimer() {
        switch (currentStage) {
            case READY:
                instructionText.setText("Get Ready...");
                startCountDown(3, () -> {
                    currentStage = Stage.LEFT_LEG;
                    startStageTimer();
                });
                break;

            case LEFT_LEG:
                instructionText.setText("Left leg up - hold balance");
                startCountDown(7, () -> {
                    currentStage = Stage.REST;
                    startStageTimer();
                });
                break;

            case REST:
                instructionText.setText("Rest");
                startCountDown(3, () -> {
                    currentStage = Stage.RIGHT_LEG;
                    startStageTimer();
                });
                break;

            case RIGHT_LEG:
                instructionText.setText("Right leg up - hold balance");
                startCountDown(7, () -> {
                    currentStage = Stage.DONE;
                    startStageTimer();
                });
                break;

            case DONE:
                instructionText.setText("Well done!");
                timerText.setText("");
                break;
        }
    }

    private void startCountDown(int seconds, Runnable onFinish) {
        timerText.setText(String.valueOf(seconds));
        new CountDownTimer(seconds * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                timerText.setText("");
                onFinish.run();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            gyroX.setText(String.format("Gyro X: %.2f", x));
            gyroY.setText(String.format("Gyro Y: %.2f", y));
            gyroZ.setText(String.format("Gyro Z: %.2f", z));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
