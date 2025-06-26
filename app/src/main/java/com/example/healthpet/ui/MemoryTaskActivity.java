package com.example.healthpet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.healthpet.R;

import java.util.ArrayList;
import java.util.Collections;

public class MemoryTaskActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView instructionText;

    private LinearLayout gameChoiceLayout;
    private Button numberGameButton, colorGameButton;

    private LinearLayout numberGameLayout;
    private LinearLayout colorGameLayout;

    // Number game variables
    private ArrayList<Integer> numberSequence = new ArrayList<>();
    private ArrayList<Button> numberButtons = new ArrayList<>();
    private int currentNumberIndex = 0;
    private boolean numberGameActive = false;

    // Color game variables
    private ArrayList<Integer> colorPairs = new ArrayList<>();
    private ArrayList<View> colorTiles = new ArrayList<>();
    private int firstColorIndex = -1;
    private boolean colorGameActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_task);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        instructionText = findViewById(R.id.instructionText);

        gameChoiceLayout = findViewById(R.id.gameChoiceLayout);
        numberGameButton = findViewById(R.id.numberGameButton);
        colorGameButton = findViewById(R.id.colorGameButton);

        numberGameLayout = findViewById(R.id.numberGameLayout);
        colorGameLayout = findViewById(R.id.colorGameLayout);

        numberGameButton.setOnClickListener(v -> startNumberGame());
        colorGameButton.setOnClickListener(v -> startColorGame());

        showIntro();
    }

    private void showIntro() {
        instructionText.setText("Choose a memory game below.");
        gameChoiceLayout.setVisibility(View.VISIBLE);
        numberGameLayout.setVisibility(View.GONE);
        colorGameLayout.setVisibility(View.GONE);
        numberGameActive = false;
        colorGameActive = false;
    }


    private void startNumberGame() {
        gameChoiceLayout.setVisibility(View.GONE);
        colorGameLayout.setVisibility(View.GONE);
        numberGameLayout.setVisibility(View.VISIBLE);
        instructionText.setText("Memorize the numbers in order!");

        numberButtons.clear();
        numberSequence.clear();
        currentNumberIndex = 0;
        numberGameActive = true;
        numberGameLayout.removeAllViews();

        for (int i = 1; i <= 5; i++) numberSequence.add(i);

        for (int num : numberSequence) {
            Button btn = createNumberButton(String.valueOf(num));
            btn.setTag(num);
            numberButtons.add(btn);
            numberGameLayout.addView(btn);
        }

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                shuffleAndHideNumbers();
            }
        }.start();
    }

    private Button createNumberButton(String text) {
        Button btn = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 150, 1);
        params.setMargins(8, 0, 8, 0);
        btn.setLayoutParams(params);
        btn.setText(text);
        btn.setTextSize(32f);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.DKGRAY);
        return btn;
    }

    private void shuffleAndHideNumbers() {
        ArrayList<Integer> originalPositions = new ArrayList<>();
        for (int i = 0; i < numberButtons.size(); i++) {
            originalPositions.add(i);
        }
        ArrayList<Integer> shuffledPositions = new ArrayList<>(originalPositions);
        Collections.shuffle(shuffledPositions);

        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animations = new ArrayList<>();

        for (int i = 0; i < numberButtons.size(); i++) {
            Button btn = numberButtons.get(i);
            int newPos = shuffledPositions.get(i);

            int distance = (newPos - i) * (btn.getWidth() + 16);
            ObjectAnimator animX = ObjectAnimator.ofFloat(btn, "translationX", btn.getTranslationX(), distance);
            animations.add(animX);
        }

        animatorSet.playTogether(animations);
        animatorSet.setDuration(800);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for (Button btn : numberButtons) {
                    btn.setText("?");
                    btn.setBackgroundColor(Color.BLUE);
                    btn.setEnabled(true);
                    btn.setOnClickListener(v -> checkNumberButton((Button) v));
                }
                instructionText.setText("Tap numbers in order: 1 → 5");
            }
        });
        animatorSet.start();
    }

    private void checkNumberButton(Button btn) {
        if (!numberGameActive) return;

        int tappedNumber = (int) btn.getTag();

        if (tappedNumber == currentNumberIndex + 1) {
            currentNumberIndex++;
            btn.setBackgroundColor(Color.GREEN);
            btn.setText(String.valueOf(tappedNumber));
            btn.setEnabled(false);

            if (currentNumberIndex == 5) {
                instructionText.setText("Congrats! You completed the Number Game.");
                numberGameActive = false;

                btn.postDelayed(this::showIntro, 2000);
            }
        } else {
            btn.setBackgroundColor(Color.RED);
            instructionText.setText("Wrong! Try again.");
            resetNumberGame();
        }
    }

    private void resetNumberGame() {
        currentNumberIndex = 0;
        for (Button btn : numberButtons) {
            btn.setBackgroundColor(Color.BLUE);
            btn.setText("?");
            btn.setEnabled(true);
        }
    }


    private void startColorGame() {
        gameChoiceLayout.setVisibility(View.GONE);
        numberGameLayout.setVisibility(View.GONE);
        colorGameLayout.setVisibility(View.VISIBLE);

        instructionText.setText("Memorize colors and find all pairs!");

        colorGameActive = true;
        firstColorIndex = -1;
        colorTiles.clear();
        colorPairs.clear();
        colorGameLayout.removeAllViews();

        // Prepare color pairs (two of each color)
        colorPairs.add(Color.RED);
        colorPairs.add(Color.RED);
        colorPairs.add(Color.BLUE);
        colorPairs.add(Color.BLUE);
        colorPairs.add(Color.GREEN);
        colorPairs.add(Color.GREEN);

        Collections.shuffle(colorPairs);

        // Create tiles as Views
        for (int i = 0; i < colorPairs.size(); i++) {
            View tile = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 200, 1);
            params.setMargins(8, 0, 8, 0);
            tile.setLayoutParams(params);
            tile.setBackgroundColor(colorPairs.get(i));
            tile.setTag("hidden"); // initially visible but will hide after timer
            final int index = i;
            tile.setOnClickListener(v -> onColorTileClicked(index));
            colorTiles.add(tile);
            colorGameLayout.addView(tile);
        }

        // Show colors for 4 seconds, then hide tiles (set black background)
        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                for (View tile : colorTiles) {
                    tile.setBackgroundColor(Color.BLACK);
                    tile.setTag("hidden");
                }
                instructionText.setText("Tap tiles to find matching colors.");
            }
        }.start();
    }

    private void onColorTileClicked(int index) {
        if (!colorGameActive) return;

        View clickedTile = colorTiles.get(index);
        String state = (String) clickedTile.getTag();

        if ("matched".equals(state) || "visible".equals(state)) {
            // Ignore clicks on matched or currently visible tiles
            return;
        }

        // Reveal clicked tile
        clickedTile.setBackgroundColor(colorPairs.get(index));
        clickedTile.setTag("visible");

        if (firstColorIndex == -1) {
            // First tile selected
            firstColorIndex = index;
        } else {
            // Second tile selected
            if (colorPairs.get(firstColorIndex).equals(colorPairs.get(index))) {
                // Match found
                colorTiles.get(firstColorIndex).setTag("matched");
                colorTiles.get(index).setTag("matched");
                instructionText.setText("Good job! Keep finding pairs.");

                // Check if all matched
                if (allColorTilesMatched()) {
                    instructionText.setText("Congrats! You found all pairs.");
                    colorGameActive = false;

                    clickedTile.postDelayed(this::showIntro, 2000);
                }
            } else {
                // No match - hide both tiles after delay
                final int firstIndex = firstColorIndex;
                final int secondIndex = index;
                clickedTile.postDelayed(() -> {
                    colorTiles.get(firstIndex).setBackgroundColor(Color.BLACK);
                    colorTiles.get(secondIndex).setBackgroundColor(Color.BLACK);
                    colorTiles.get(firstIndex).setTag("hidden");
                    colorTiles.get(secondIndex).setTag("hidden");
                }, 1000);
            }
            firstColorIndex = -1;
        }
    }

    private boolean allColorTilesMatched() {
        for (View tile : colorTiles) {
            if (!"matched".equals(tile.getTag())) {
                return false;
            }
        }
        return true;
    }
}
