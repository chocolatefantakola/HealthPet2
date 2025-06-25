package com.example.healthpet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("stepPrefs", Context.MODE_PRIVATE);
        prefs.edit().putFloat("initialStepCount", -1).apply(); // Reset initialStepCount
        prefs.edit().putInt("stepsToday", 0).apply();
    }
}
