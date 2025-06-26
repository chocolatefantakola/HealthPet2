package com.example.healthpet.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.example.healthpet.R;


public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private float initialStepCount = -1;
    private static final int dailyStepGoal = 10000;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        startForegroundService();
        loadInitialStepCount();
    }

    private void startForegroundService() {
        String channelId = "StepCounterChannel";
        String channelName = "Step Counter";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Step Counter")
                .setContentText("Counting your steps...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float totalStepsSinceReboot = event.values[0];

        if (initialStepCount < 0) {
            initialStepCount = totalStepsSinceReboot;
            saveInitialStepCount(initialStepCount);
        }

        int stepsToday = (int)(totalStepsSinceReboot - initialStepCount);
        if (stepsToday < 0) stepsToday = 0;

        saveStepsToday(stepsToday);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Speicher Methoden
    private void saveInitialStepCount(float value) {
        SharedPreferences prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE);
        prefs.edit().putFloat("initialStepCount", value).apply();
    }

    private void saveStepsToday(int stepsToday) {
        SharedPreferences prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE);
        prefs.edit().putInt("stepsToday", stepsToday).apply();
    }

    private void loadInitialStepCount() {
        SharedPreferences prefs = getSharedPreferences("stepPrefs", MODE_PRIVATE);
        initialStepCount = prefs.getFloat("initialStepCount", -1);
    }
}