package com.example.healthpet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthpet.R;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioGroup ageGroupRadioGroup, sexRadioGroup, respiratoryRadioGroup;
    private EditText heightInput, weightInput;
    private Button submitButton;

    private static final String TAG = "QuestionnaireActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // Find views
        ageGroupRadioGroup = findViewById(R.id.ageGroupRadioGroup);
        sexRadioGroup = findViewById(R.id.sexRadioGroup);
        respiratoryRadioGroup = findViewById(R.id.respiratoryRadioGroup);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        submitButton = findViewById(R.id.submitQuestionnaireButton);

        submitButton.setOnClickListener(v -> {
            if (validateInput()) {

                String ageGroup = ((RadioButton) findViewById(ageGroupRadioGroup.getCheckedRadioButtonId())).getText().toString();
                String sex = ((RadioButton) findViewById(sexRadioGroup.getCheckedRadioButtonId())).getText().toString();
                String respiratory = ((RadioButton) findViewById(respiratoryRadioGroup.getCheckedRadioButtonId())).getText().toString();
                String height = heightInput.getText().toString().trim();
                String weight = weightInput.getText().toString().trim();

                SharedPreferences prefs = getSharedPreferences("HealthPetPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();


                if (respiratory.equalsIgnoreCase("Yes")) {
                    editor.putBoolean("breathingBlocked", true);
                    Toast.makeText(this, "Breathing task is blocked due to respiratory issues.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Respiratory issue detected. Blocking breathing task.");
                } else {
                    editor.putBoolean("breathingBlocked", false);
                    Toast.makeText(this, "Questionnaire submitted successfully.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No respiratory issues. Breathing task allowed.");
                }

                editor.apply();

                Intent intent = new Intent(QuestionnaireActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInput() {
        if (ageGroupRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select your age group", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sexRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select your sex", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (heightInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (weightInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (respiratoryRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please answer the respiratory question", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
