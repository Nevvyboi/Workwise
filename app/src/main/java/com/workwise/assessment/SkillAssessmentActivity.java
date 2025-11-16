package com.workwise.assessment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;

public class SkillAssessmentActivity extends AppCompatActivity {

    private RadioGroup question1Group, question2Group, question3Group;
    private MaterialButton submitButton;
    private MaterialCardView resultsCard;
    private TextView resultsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_assessment);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle back button click
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Find Views
        question1Group = findViewById(R.id.question1Group);
        question2Group = findViewById(R.id.question2Group);
        question3Group = findViewById(R.id.question3Group);
        submitButton = findViewById(R.id.submitAssessmentButton);
        resultsCard = findViewById(R.id.resultsCard);
        resultsText = findViewById(R.id.resultsText);

        submitButton.setOnClickListener(v -> calculateResults());
    }

    private void calculateResults() {
        int score = 0;
        int totalQuestions = 3;

        // Check answers (example logic)
        if (question1Group.getCheckedRadioButtonId() == R.id.q1_option2) {
            score++;
        }
        if (question2Group.getCheckedRadioButtonId() == R.id.q2_option3) {
            score++;
        }
        if (question3Group.getCheckedRadioButtonId() == R.id.q3_option1) {
            score++;
        }

        // Display results
        String resultMessage = "You scored " + score + " out of " + totalQuestions + ".\n\n";
        if (score == totalQuestions) {
            resultMessage += "Excellent! You have a strong understanding of project management.";
        } else if (score >= 1) {
            resultMessage += "Good job! We recommend brushing up on the areas you missed.";
        } else {
            resultMessage += "No worries! We have resources to help you learn these concepts.";
        }

        resultsText.setText(resultMessage);
        resultsCard.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Assessment Submitted!", Toast.LENGTH_SHORT).show();
    }
}
