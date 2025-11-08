package com.workwise;

import com.workwise.ui.bottomNav;
import com.workwise.settings.*;
import com.workwise.models.cvItem;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class setting extends bottomNav {

    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private TextView profileBio;
    private TextView applicationsCount;
    private TextView savedJobsCount;
    private TextView cvBadgeText;
    private MaterialCardView cvBadge;

    private MaterialCardView manageProfileCard;
    private MaterialCardView manageCvCard;
    private MaterialCardView manageQualificationsCard;
    private MaterialCardView viewSavedJobsCard;
    private MaterialButton signOutButton;

    private apiService api;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Initialize API
        api = apiClient.get().create(apiService.class);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        initializeViews();
        setupClickListeners();
        loadUserData();
        checkCVStatus();
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

        // CV Badge
        cvBadge = findViewById(R.id.cvBadge);
        cvBadgeText = findViewById(R.id.cvBadgeText);

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

        if (signOutButton != null) {
            signOutButton.setOnClickListener(v -> handleSignOut());
        }
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);

        String userName = prefs.getString("user_name", "User Name");
        String userEmail = prefs.getString("user_email", "user@workwise.za");
        String userBio = prefs.getString("user_bio", "Bio...");

        if (profileName != null) profileName.setText(userName);
        if (profileEmail != null) profileEmail.setText(userEmail);
        if (profileBio != null) profileBio.setText(userBio);

        if (applicationsCount != null) applicationsCount.setText("24");
        if (savedJobsCount != null) savedJobsCount.setText("4");
    }

    private void checkCVStatus() {
        if (userId == -1 || cvBadge == null || cvBadgeText == null) {
            updateCVBadge(false);
            return;
        }

        Call<List<cvItem>> call = api.getCVs(userId, apiConfig.tokenCvList);

        call.enqueue(new Callback<List<cvItem>>() {
            @Override
            public void onResponse(Call<List<cvItem>> call, Response<List<cvItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<cvItem> cvs = response.body();
                    updateCVBadge(!cvs.isEmpty());
                } else {
                    updateCVBadge(false);
                }
            }

            @Override
            public void onFailure(Call<List<cvItem>> call, Throwable t) {
                updateCVBadge(false);
            }
        });
    }

    private void updateCVBadge(boolean hasCv) {
        runOnUiThread(() -> {
            if (cvBadge != null && cvBadgeText != null) {
                cvBadge.setVisibility(View.VISIBLE);

                if (hasCv) {
                    cvBadgeText.setText("CV: ✓");
                    cvBadge.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                    cvBadgeText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    cvBadgeText.setText("CV: ✗");
                    cvBadge.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                    cvBadgeText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });
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
        getSharedPreferences("WorkWisePrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(this, authentication.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        checkCVStatus();
    }
}