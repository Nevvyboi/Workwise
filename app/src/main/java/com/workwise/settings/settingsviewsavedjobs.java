package com.workwise.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;

public class settingsviewsavedjobs extends AppCompatActivity {

    private ImageButton backButton;
    private MaterialCardView emptyStateCard;
    private LinearLayout savedJobsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsviewsavedjobs);

        initializeViews();
        setupClickListeners();
        loadSavedJobs();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        savedJobsContainer = findViewById(R.id.savedJobsContainer);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void loadSavedJobs() {
        // TODO: Load saved jobs from database
        // For now, show empty state
        emptyStateCard.setVisibility(View.VISIBLE);
        savedJobsContainer.setVisibility(View.GONE);

        // If there are saved jobs, do this instead:
        // emptyStateCard.setVisibility(View.GONE);
        // savedJobsContainer.setVisibility(View.VISIBLE);
        // populateSavedJobs();
    }

    private void populateSavedJobs() {
        // TODO: Add saved job cards dynamically
    }
}