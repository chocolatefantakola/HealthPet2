package com.example.healthpet.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthpet.R;
import com.example.healthpet.data.AppDatabase;
import com.example.healthpet.model.TaskCompletion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class MemoryTaskActivity extends AppCompatActivity {

    private GridLayout memoryGrid;
    private TextView instructionText;
    private ArrayList<Integer> cardValues = new ArrayList<>();
    private ArrayList<ImageView> tiles = new ArrayList<>();
    private int firstIndex = -1;
    private boolean busy = false;

    private final int[] placeholderImages = {
            R.drawable.koala_memory1,
            R.drawable.koala_memory2,
            R.drawable.koala_memory3,
            R.drawable.koala_memory4,
            R.drawable.koala_memory5,
            R.drawable.koala_memory6
    };

    private final int cardBack = R.drawable.koala_abgeschnitten;

    private static final String PREFS_NAME = "MemoryPrefs";
    private static final String KEY_LAST_DONE = "lastMemoryDone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_task);

        instructionText = findViewById(R.id.instructionText);
        memoryGrid = findViewById(R.id.memoryGrid);
        LottieAnimationView koalaView = findViewById(R.id.koalaView);

        koalaView.setAnimation("Koala_Breathing.json");
        koalaView.playAnimation();

        if (isTaskCompletedToday()) {
            blockTask();
        } else {
            setupGame();
        }
    }

    private void setupGame() {
        cardValues.clear();
        for (int i = 0; i < 6; i++) {
            cardValues.add(i);
            cardValues.add(i);
        }
        Collections.shuffle(cardValues);

        memoryGrid.removeAllViews();
        tiles.clear();

        for (int i = 0; i < 12; i++) {
            ImageView card = new ImageView(this);
            card.setImageResource(cardBack);
            card.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            card.setLayoutParams(params);

            final int index = i;
            card.setOnClickListener(v -> onCardClicked(index));

            tiles.add(card);
            memoryGrid.addView(card);
        }
    }

    private void onCardClicked(int index) {
        if (busy) return;
        ImageView clickedCard = tiles.get(index);

        if ("matched".equals(clickedCard.getTag()) || "visible".equals(clickedCard.getTag())) return;

        clickedCard.setImageResource(placeholderImages[cardValues.get(index)]);
        clickedCard.setTag("visible");

        if (firstIndex == -1) {
            firstIndex = index;
        } else {
            if (cardValues.get(firstIndex).equals(cardValues.get(index)) && firstIndex != index) {
                clickedCard.setTag("matched");
                tiles.get(firstIndex).setTag("matched");
                firstIndex = -1;
                if (checkWin()) {
                    onGameCompleted();
                }
            } else {
                busy = true;
                final int prevIndex = firstIndex;
                firstIndex = -1;
                new Handler().postDelayed(() -> {
                    clickedCard.setImageResource(cardBack);
                    clickedCard.setTag("hidden");
                    tiles.get(prevIndex).setImageResource(cardBack);
                    tiles.get(prevIndex).setTag("hidden");
                    busy = false;
                }, 1000);
            }
        }
    }

    private boolean checkWin() {
        for (ImageView tile : tiles) {
            if (!"matched".equals(tile.getTag())) {
                return false;
            }
        }
        return true;
    }

    private void onGameCompleted() {
        instructionText.setText("🎉 All pairs found!");
        saveLastDoneTime();
        saveTaskCompletion();

        new AlertDialog.Builder(this)
                .setTitle("Congratulations!")
                .setMessage("You completed the Memory Game!")
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveLastDoneTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_DONE, System.currentTimeMillis()).apply();
    }

    private boolean isTaskCompletedToday() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastDoneMillis = prefs.getLong(KEY_LAST_DONE, 0);
        if (lastDoneMillis == 0) return false;

        Calendar now = Calendar.getInstance();
        Calendar lastDone = Calendar.getInstance();
        lastDone.setTimeInMillis(lastDoneMillis);

        return isSameDay(now, lastDone) && now.get(Calendar.HOUR_OF_DAY) >= 7;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void blockTask() {
        for (ImageView tile : tiles) {
            tile.setEnabled(false);
        }
        instructionText.setText("✅ Already completed today!");
        Toast.makeText(this, "You’ve already completed this today. Come back tomorrow after 7 AM.", Toast.LENGTH_LONG).show();
    }

    private void saveTaskCompletion() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "task-database")
                    .fallbackToDestructiveMigration()
                    .build();
            db.taskDao().insert(new TaskCompletion("Memory", System.currentTimeMillis()));
        }).start();
    }
}
