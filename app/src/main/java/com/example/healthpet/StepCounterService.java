package com.example.healthpet;

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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class StepCounterService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "StepCounterChannel";
    private static final int STEP_GOAL = 10000;
    private static final String PREFS_NAME = "StepGoalPrefs";
    private static final String PREF_LAST_RESET_DAY = "lastResetDay";
    private static final String PREF_STEPS_TODAY = "stepsToday";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SharedPreferences prefs;
    private int stepsToday;

    private Handler resetHandler = new Handler();
    private Runnable resetRunnable;

    private static final float THRESHOLD = 11.0f;
    private static final int STEP_DELAY_MS = 500;
    private long lastStepTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        stepsToday = prefs.getInt(PREF_STEPS_TODAY, 0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        resetRunnable = () -> {
            checkAndResetDailySteps();
            resetHandler.postDelayed(resetRunnable, 60 * 1000);
        };
        resetHandler.post(resetRunnable);
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("HealthPet Schrittzähler")
                .setContentText("Schritte werden gezählt...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        resetHandler.removeCallbacks(resetRunnable);
        saveData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double magnitude = Math.sqrt(x * x + y * y + z * z);
        long currentTime = System.currentTimeMillis();

        if (magnitude > THRESHOLD) {
            if (currentTime - lastStepTime > STEP_DELAY_MS) {
                stepsToday++;
                lastStepTime = currentTime;
                saveData();
                Log.d("StepCounterService", "Steps: " + stepsToday);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void checkAndResetDailySteps() {
        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_YEAR);
        int lastResetDay = prefs.getInt(PREF_LAST_RESET_DAY, -1);

        if (lastResetDay != today && now.get(Calendar.HOUR_OF_DAY) >= 7) {
            stepsToday = 0;
            saveData();
            prefs.edit().putInt(PREF_LAST_RESET_DAY, today).apply();
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_STEPS_TODAY, stepsToday);
        editor.apply();
    }
}
