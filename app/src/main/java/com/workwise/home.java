package com.workwise;

import com.workwise.ui.bottomNav;

import android.os.Bundle;
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
                Toast.makeText(this, "Opening Interview Tips...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to interview tips
            });
        }

        MaterialCardView skillAssCard = findViewById(R.id.skillAssCard);
        if (skillAssCard != null) {
            skillAssCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Skill Assessment...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to skill assessment
            });
        }

        // Featured Job Listings
        MaterialCardView featuredJobCard = findViewById(R.id.featuredJobCard);
        if (featuredJobCard != null) {
            featuredJobCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Featured Jobs...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to featured jobs
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
}