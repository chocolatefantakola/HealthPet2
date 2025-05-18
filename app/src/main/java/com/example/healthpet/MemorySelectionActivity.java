package com.example.healthpet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MemorySelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_selection);

        findViewById(R.id.colorMatchButton).setOnClickListener(v -> {
            startActivity(new Intent(this, ColorMatchActivity.class));
        });

        findViewById(R.id.numberOrderButton).setOnClickListener(v -> {
            startActivity(new Intent(this, NumberOrderActivity.class));
        });

        findViewById(R.id.backArrow).setOnClickListener(v -> finish());
    }
}
