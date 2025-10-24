package com.workwise;

import com.workwise.ui.bottomNav;
import com.workwise.settings.*;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class setting extends bottomNav {

    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private TextView profileBio;
    private TextView applicationsCount;
    private TextView savedJobsCount;

    private MaterialCardView manageProfileCard;
    private MaterialCardView manageCvCard;
    private MaterialCardView manageQualificationsCard;
    private MaterialCardView viewSavedJobsCard;
    private MaterialButton signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        initializeViews();
        setupClickListeners();
        loadUserData();
    }

    @Override
    protected String getCurrentNavItem() {
        return "setting";
    }

    private void initializeViews() {
        // Profile components
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileBio = findViewById(R.id.profileBio);

        // Stats
        applicationsCount = findViewById(R.id.applicationsCount);
        savedJobsCount = findViewById(R.id.savedJobsCount);

        // Menu cards
        manageProfileCard = findViewById(R.id.manageProfileCard);
        manageCvCard = findViewById(R.id.manageCvCard);
        manageQualificationsCard = findViewById(R.id.manageQualificationsCard);
        viewSavedJobsCard = findViewById(R.id.viewSavedJobsCard);
        signOutButton = findViewById(R.id.signOutButton);
    }

    private void setupClickListeners() {
        // Manage Profile
        if (manageProfileCard != null) {
            manageProfileCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsprofile.class);
                startActivity(intent);
            });
        }

        // Manage CV
        if (manageCvCard != null) {
            manageCvCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsmanagecv.class);
                startActivity(intent);
            });
        }

        // Manage Qualifications
        if (manageQualificationsCard != null) {
            manageQualificationsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsqualifications.class);
                startActivity(intent);
            });
        }

        // View Saved Jobs
        if (viewSavedJobsCard != null) {
            viewSavedJobsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsviewsavedjobs.class);
                startActivity(intent);
                finish();
            });
        }

        // Sign Out
        if (signOutButton != null) {
            signOutButton.setOnClickListener(v -> handleSignOut());
        }
    }

    private void loadUserData() {
        // TODO: Load actual user data from SharedPreferences, Database, or API
        if (profileName != null) profileName.setText("panayioti economou");
        if (profileEmail != null) profileEmail.setText("pano@workwise.za");
        if (profileBio != null) profileBio.setText("hi I'm pano a software engineer");

        if (applicationsCount != null) applicationsCount.setText("24");
        if (savedJobsCount != null) savedJobsCount.setText("4");
    }

    private void handleSignOut() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> performSignOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSignOut() {
        // TODO: Clear user session
        // Clear SharedPreferences
        // getSharedPreferences("UserPrefs", MODE_PRIVATE)
        //     .edit()
        //     .clear()
        //     .apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(this, authentication.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}