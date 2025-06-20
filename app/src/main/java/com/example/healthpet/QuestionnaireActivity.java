package com.example.healthpet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioGroup ageGroupRadioGroup, sexRadioGroup, respiratoryRadioGroup;
    private EditText heightInput, weightInput;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

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


                Toast.makeText(this, "Questionnaire submitted!", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, "Please answer the respiratory problem question", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
