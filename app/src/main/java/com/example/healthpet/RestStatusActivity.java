package com.example.healthpet;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RestStatusActivity extends AppCompatActivity {

    private TextView restStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_status);

        restStatusTextView = findViewById(R.id.restStatusTextView);

        boolean movedLastNight = getSharedPreferences("SleepPrefs", MODE_PRIVATE)
                .getBoolean("movedLastNight", false);

        if (movedLastNight) {
            restStatusTextView.setText("You moved during the night. Try to rest more.");
        } else {
            restStatusTextView.setText("Good rest! You stayed still overnight.");
        }
    }
}
