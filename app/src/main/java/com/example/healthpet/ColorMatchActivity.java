package com.example.healthpet;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorMatchActivity extends AppCompatActivity {

    private GridLayout colorGrid;
    private TextView instructionText;
    private List<Integer> colorSequence = new ArrayList<>();
    private int[] colorValues = {R.color.red, R.color.green, R.color.blue, R.color.yellow};
    private int currentIndex = 0;
    private boolean showingSequence = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_match);

        colorGrid = findViewById(R.id.colorGrid);
        instructionText = findViewById(R.id.instructionText);

        setupGame();
    }

    private void setupGame() {
        colorSequence.clear();
        for (int color : colorValues) colorSequence.add(color);
        Collections.shuffle(colorSequence);

        instructionText.setText("Memorize the colors...");
        new Handler().postDelayed(() -> {
            displayColorButtons();
            showingSequence = false;
            instructionText.setText("Tap in the same order!");
        }, 3000);
    }

    private void displayColorButtons() {
        colorGrid.removeAllViews();
        for (int i = 0; i < colorValues.length; i++) {
            View button = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            params.setMargins(16, 16, 16, 16);
            button.setLayoutParams(params);
            button.setBackgroundResource(colorValues[i]);

            final int finalI = i;
            button.setOnClickListener(v -> {
                if (!showingSequence) {
                    if (colorValues[finalI] == colorSequence.get(currentIndex)) {
                        currentIndex++;
                        if (currentIndex == colorSequence.size()) {
                            instructionText.setText("🎉 Correct Order! Well done!");
                        }
                    } else {
                        Toast.makeText(this, "Wrong order!", Toast.LENGTH_SHORT).show();
                        setupGame(); // Restart
                    }
                }
            });

            colorGrid.addView(button);
        }
    }
}
