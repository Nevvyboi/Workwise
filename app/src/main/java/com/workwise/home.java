package com.workwise;

import com.google.android.material.button.MaterialButton;
import com.workwise.ui.bottomNav;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;

public class home extends bottomNav {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        setupClickListeners();
    }

    @Override
    protected String getCurrentNavItem() {
        return "home";
    }

    private void setupClickListeners() {
        // Featured Action Button
        MaterialCardView featuredCard = findViewById(R.id.featuredCard);
        if (featuredCard != null) {
            featuredCard.setOnClickListener(v -> {
                Toast.makeText(this, "Find Your Next Opportunity", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to opportunities screen
            });
        }

        // Quick Action Cards
        MaterialCardView jobSearchCard = findViewById(R.id.jobSearchCard);
        if (jobSearchCard != null) {
            jobSearchCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Job Search...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to job search
            });
        }

        MaterialCardView cvBuilderCard = findViewById(R.id.cvBuilderCard);
        if (cvBuilderCard != null) {
            cvBuilderCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening CV Builder...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to CV builder
            });
        }

        MaterialCardView interviewCard = findViewById(R.id.interviewCard);
        if (interviewCard != null) {
            interviewCard.setOnClickListener(v -> {
                String randomTip = getRandomInterviewTip();
                showInterviewTipPopup(randomTip);
            });
        }

        MaterialCardView skillAssCard = findViewById(R.id.skillAssCard);
        if (skillAssCard != null) {
            skillAssCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Skill Assessment...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to skill assessment
            });
        }

        // Top bar buttons
        findViewById(R.id.expandButton).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Menu...", Toast.LENGTH_SHORT).show();
            // TODO: Open navigation drawer or menu
        });

        findViewById(R.id.profileButton).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to profile
        });
    }

    private final String[] interviewTips = new String[]{
            "Research the company and role so you can link your answers to their goals.",
            "Prepare 2–3 strong stories using the STAR method (Situation, Task, Action, Result).",
            "Practice answering: 'Tell me about yourself' in under 90 seconds.",
            "Know your CV well — be ready to explain every project and achievement confidently.",
            "Prepare at least three smart questions to ask the interviewer.",
            "Arrive 10–15 minutes early, calm, and composed. Rushing = scattered answers.",
            "Match your examples to the job description: show you're already solving their problems.",
            "If you don’t know an answer, be honest and think out loud instead of freezing.",
            "Make eye contact, sit upright, and don’t underestimate the power of a genuine smile.",
            "Always close by thanking them and reaffirming your interest in the role."
    };

    private String getRandomInterviewTip() {
        java.util.Random random = new java.util.Random();
        int index = random.nextInt(interviewTips.length);
        return interviewTips[index];
    }

    private void showInterviewTipPopup(String tip) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.interviewtip, null);

        TextView tipTextView = dialogView.findViewById(R.id.msg4);
        tipTextView.setText(tip);

        MaterialButton closeButton = dialogView.findViewById(R.id.closeButton);

        androidx.appcompat.app.AlertDialog dialog =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}