package com.example.healthpet;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberOrderActivity extends AppCompatActivity {

    private GridLayout numberGrid;
    private TextView instructionText;
    private List<Integer> numberSequence = new ArrayList<>();
    private int currentIndex = 1;
    private boolean showingSequence = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_order);

        numberGrid = findViewById(R.id.numberGrid);
        instructionText = findViewById(R.id.instructionText);

        setupGame();
    }

    private void setupGame() {
        numberSequence.clear();
        for (int i = 1; i <= 6; i++) {
            numberSequence.add(i);
        }
        Collections.shuffle(numberSequence);
        currentIndex = 1;
        showingSequence = true;

        instructionText.setText("Memorize the order...");

        new Handler().postDelayed(() -> {
            showingSequence = false;
            instructionText.setText("Tap 1 to 6 in order!");
            displayNumberButtons();
        }, 3000);
    }

    private void displayNumberButtons() {
        numberGrid.removeAllViews();

        for (int number : numberSequence) {
            TextView numberBox = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            params.setMargins(16, 16, 16, 16);
            numberBox.setLayoutParams(params);
            numberBox.setBackgroundResource(R.drawable.number_box_background);
            numberBox.setText(String.valueOf(number));
            numberBox.setGravity(Gravity.CENTER);
            numberBox.setTextSize(24);
            numberBox.setTextColor(getResources().getColor(android.R.color.black));

            numberBox.setOnClickListener(v -> {
                if (!showingSequence) {
                    int tapped = Integer.parseInt(((TextView) v).getText().toString());
                    if (tapped == currentIndex) {
                        currentIndex++;
                        if (currentIndex > 6) {
                            instructionText.setText("🎉 Great job!");
                        }
                    } else {
                        Toast.makeText(this, "Wrong number!", Toast.LENGTH_SHORT).show();
                        setupGame();
                    }
                }
            });

            numberGrid.addView(numberBox);
        }
    }
}
