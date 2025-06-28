package com.example.healthpet.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyResetManager {

    private static final String PREFS_NAME = "DailyPrefs";
    private static final String KEY_LAST_RESET_DATE = "lastResetDate";

    public static void checkAndResetIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "");
        String today = getTodayDateString();

        if (!today.equals(lastResetDate)) {
            performReset(context);
            prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply();
        }
    }

    private static void performReset(Context context) {

        SharedPreferences waterPrefs = context.getSharedPreferences("WaterPrefs", Context.MODE_PRIVATE);
        waterPrefs.edit().putLong("waterAmount", 0).putBoolean("goalReached", false).apply();


        SharedPreferences stepPrefs = context.getSharedPreferences("StepPrefs", Context.MODE_PRIVATE);
        stepPrefs.edit().putLong("stepCount", 0).apply();


    }

    private static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
