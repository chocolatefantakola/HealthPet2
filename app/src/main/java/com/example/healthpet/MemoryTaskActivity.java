package com.example.healthpet;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MemoryTaskActivity extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private TextView instructionText, timerText;
    private Toolbar toolbar;

    private final int CIRCLE_COUNT = 6;
    private final int CIRCLE_SIZE_DP = 70;

    private ArrayList<TextView> circles = new ArrayList<>();
    private int nextNumberToSelect = 1;

    private final long SHOW_NUMBERS_TIME = 10_000; // 10 seconds to memorize
    private final long SHUFFLE_DURATION = 4_000; // 4 seconds shuffle animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_task);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rootLayout = findViewById(R.id.rootLayout);
        instructionText = findViewById(R.id.instructionText);
        timerText = findViewById(R.id.timerText);

        startInstructionTimer();
    }

    private void startInstructionTimer() {
        instructionText.setText("Remember the numbers!");
        timerText.setVisibility(View.VISIBLE);

        new CountDownTimer(SHOW_NUMBERS_TIME, 500) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                timerText.setVisibility(View.GONE);
                setupCircles();
                showNumbers();
                startShowNumbersTimer();
            }
        }.start();
    }

    private void setupCircles() {
        int circleSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_SIZE_DP, getResources().getDisplayMetrics());
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        rootLayout.removeAllViews();

        // Add toolbar, instructionText, timerText back to rootLayout
        rootLayout.addView(toolbar);
        rootLayout.addView(instructionText);
        rootLayout.addView(timerText);

        circles.clear();
        Random random = new Random();

        for (int i = 1; i <= CIRCLE_COUNT; i++) {
            TextView circle = new TextView(this);
            circle.setText(String.valueOf(i));
            circle.setTextColor(Color.WHITE);
            circle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            circle.setBackgroundColor(Color.BLUE);
            circle.setGravity(Gravity.CENTER);
            circle.setId(View.generateViewId());
            circle.setTag(i);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(circleSizePx, circleSizePx);

            // Keep circles inside visible screen area (avoid status bar and toolbar)
            int maxX = screenWidth - circleSizePx - 50;
            int maxY = screenHeight - circleSizePx - 300; // give extra bottom margin

            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);

            params.leftMargin = x;
            params.topMargin = y;

            rootLayout.addView(circle, params);
            circles.add(circle);

            circle.setEnabled(false);
        }
    }

    private void showNumbers() {
        instructionText.setText("Memorize the numbers!");
        for (TextView circle : circles) {
            circle.setText(String.valueOf(circle.getTag()));
            circle.setBackgroundColor(Color.BLUE);
            circle.setEnabled(false);
            circle.setVisibility(View.VISIBLE);
        }
    }

    private void startShowNumbersTimer() {
        new CountDownTimer(SHOW_NUMBERS_TIME, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setVisibility(View.VISIBLE);
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                timerText.setVisibility(View.GONE);
                hideNumbers();
                shuffleCirclesSlowly(() -> {
                    enableCirclesForSelection();
                    instructionText.setText("Tap numbers in order!");
                });
            }
        }.start();
    }

    private void hideNumbers() {
        for (TextView circle : circles) {
            circle.setText("");
            circle.setBackgroundColor(Color.BLACK);
            circle.setEnabled(false);
            circle.setVisibility(View.VISIBLE);
        }
    }

    private void shuffleCirclesSlowly(Runnable onComplete) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int circleSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_SIZE_DP, getResources().getDisplayMetrics());

        Random random = new Random();

        ArrayList<Position> targetPositions = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            int maxX = screenWidth - circleSizePx - 50;
            int maxY = screenHeight - circleSizePx - 300;

            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);
            targetPositions.add(new Position(x, y));
        }

        Collections.shuffle(targetPositions);

        final int steps = 60;
        final long frameDuration = SHUFFLE_DURATION / steps;

        ArrayList<Position> startPositions = new ArrayList<>();
        for (TextView circle : circles) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) circle.getLayoutParams();
            startPositions.add(new Position(params.leftMargin, params.topMargin));
        }

        final int[] currentStep = {0};

        Runnable frameRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep[0] > steps) {
                    for (int i = 0; i < circles.size(); i++) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) circles.get(i).getLayoutParams();
                        params.leftMargin = targetPositions.get(i).x;
                        params.topMargin = targetPositions.get(i).y;
                        circles.get(i).setLayoutParams(params);
                    }
                    onComplete.run();
                    return;
                }

                float fraction = (float) currentStep[0] / steps;
                for (int i = 0; i < circles.size(); i++) {
                    int startX = startPositions.get(i).x;
                    int startY = startPositions.get(i).y;
                    int endX = targetPositions.get(i).x;
                    int endY = targetPositions.get(i).y;

                    int newX = (int) (startX + fraction * (endX - startX));
                    int newY = (int) (startY + fraction * (endY - startY));

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) circles.get(i).getLayoutParams();
                    params.leftMargin = newX;
                    params.topMargin = newY;
                    circles.get(i).setLayoutParams(params);
                }
                currentStep[0]++;
                rootLayout.postDelayed(this, frameDuration);
            }
        };

        rootLayout.post(frameRunnable);
    }

    private void enableCirclesForSelection() {
        nextNumberToSelect = 1;
        for (TextView circle : circles) {
            circle.setEnabled(true);
            circle.setOnClickListener(v -> {
                int clickedNumber = (int) v.getTag();
                if (clickedNumber == nextNumberToSelect) {
                    v.setBackgroundColor(Color.GREEN);
                    nextNumberToSelect++;
                    if (nextNumberToSelect > CIRCLE_COUNT) {
                        finish();
                    }
                } else {
                    v.setBackgroundColor(Color.RED);
                }
            });
        }
    }

    private static class Position {
        int x, y;
        Position(int x, int y) { this.x = x; this.y = y; }
    }
}
