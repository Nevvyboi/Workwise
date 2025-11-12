package com.workwise;

import com.workwise.models.savedJobs;
import com.workwise.ui.bottomNav;
import com.workwise.settings.*;
import com.workwise.models.cvItem;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.models.userProfile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // <-- Added for logging
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
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
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileBio = findViewById(R.id.profileBio);

        applicationsCount = findViewById(R.id.applicationsCount);
        savedJobsCount = findViewById(R.id.savedJobsCount);

        cvBadge = findViewById(R.id.cvBadge);
        cvBadgeText = findViewById(R.id.cvBadgeText);

        manageProfileCard = findViewById(R.id.manageProfileCard);
        manageCvCard = findViewById(R.id.manageCvCard);
        manageQualificationsCard = findViewById(R.id.manageQualificationsCard);
        viewSavedJobsCard = findViewById(R.id.viewSavedJobsCard);
        signOutButton = findViewById(R.id.signOutButton);
    }

    private void setupClickListeners() {
        if (manageProfileCard != null) {
            manageProfileCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsprofile.class);
                startActivity(intent);
            });
        }

        if (manageCvCard != null) {
            manageCvCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsmanagecv.class);
                startActivity(intent);
            });
        }

        if (manageQualificationsCard != null) {
            manageQualificationsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, settingsqualifications.class);
                startActivity(intent);
            });
        }

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
        loadFromSharedPreferences();

        if (userId == -1) return;

        Call<userProfile> call = api.getProfile(userId, apiConfig.tokenProfileGet);
        call.enqueue(new Callback<userProfile>() {
            @Override
            public void onResponse(Call<userProfile> call, Response<userProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userProfile profile = response.body();
                    updateUI(profile);
                    saveToSharedPreferences(profile);
                    fetchStats();
                } else {
                    Toast.makeText(setting.this, "Failed to sync profile from database. Using cached data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<userProfile> call, Throwable t) {
                Toast.makeText(setting.this, "Network error during sync: " + t.getMessage() + ". Using cached data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User Name");
        String userEmail = prefs.getString("user_email", "user@workwise.za");
        String userBio = prefs.getString("user_bio", "Bio...");
        String userProfileImage = prefs.getString("user_profile_image", null);

        if (profileName != null) profileName.setText(userName);
        if (profileEmail != null) profileEmail.setText(userEmail);
        if (profileBio != null) profileBio.setText(userBio);

        if (profileImage != null && userProfileImage != null && !userProfileImage.isEmpty()) {
            Glide.with(this)
                    .load(userProfileImage)
                    .placeholder(R.drawable.outlineaccountscircle24)
                    .circleCrop()
                    .into(profileImage);
        } else if (profileImage != null) {
            profileImage.setImageResource(R.drawable.outlineaccountscircle24);
        }

        if (applicationsCount != null) applicationsCount.setText(prefs.getString("applications_count", "0"));
        if (savedJobsCount != null) savedJobsCount.setText(prefs.getString("saved_jobs_count", "0"));
    }

    private void updateUI(userProfile profile) {
        runOnUiThread(() -> {
            if (profileName != null) profileName.setText(profile.profileName != null ? profile.profileName : "User Name");
            if (profileEmail != null) profileEmail.setText(profile.email != null ? profile.email : "user@workwise.za");
            if (profileBio != null) profileBio.setText(profile.profileBio != null ? profile.profileBio : "Bio...");

            String imageUrl = profile.profileImage;
            if (imageUrl != null && !imageUrl.isEmpty() && profileImage != null) {
                imageUrl = imageUrl.replace("\\", "/");
                if (imageUrl.startsWith("/")) imageUrl = imageUrl.substring(1);
                String baseUrl = apiConfig.baseUrl;
                if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                imageUrl = baseUrl + "/" + imageUrl;

                Glide.with(setting.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.outlineaccountscircle24)
                        .error(R.drawable.outlineaccountscircle24)
                        .circleCrop()
                        .into(profileImage);
            } else if (profileImage != null) {
                profileImage.setImageResource(R.drawable.outlineaccountscircle24);
            }
        });
    }

    private void saveToSharedPreferences(userProfile profile) {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", profile.profileName != null ? profile.profileName : "");
        editor.putString("user_email", profile.email != null ? profile.email : "");
        editor.putString("user_bio", profile.profileBio != null ? profile.profileBio : "");
        editor.putString("user_profile_image", profile.profileImage != null ? profile.profileImage : "");
        editor.apply();
    }

    // FIXED: Removed duplicate nested method, corrected generics
    private void fetchStats() {
        Call<List<savedJobs>> savedJobsCall = api.getSavedJobs(userId, apiConfig.tokenSavedList);
        savedJobsCall.enqueue(new Callback<List<savedJobs>>() {
            @Override
            public void onResponse(Call<List<savedJobs>> call, Response<List<savedJobs>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    if (savedJobsCount != null) {
                        savedJobsCount.setText(String.valueOf(count));
                    }
                    saveStatToPrefs("saved_jobs_count", String.valueOf(count));
                }
            }

            @Override
            public void onFailure(Call<List<savedJobs>> call, Throwable t) {
                Log.e("SETTING_STATS", "Failed to fetch saved-jobs count: " + t.getMessage());
            }
        });

        // TODO: Add applications count endpoint when available
    }

    private void saveStatToPrefs(String key, String value) {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
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
                    updateCVBadge(!response.body().isEmpty());
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
                    cvBadgeText.setText("CV: Checkmark");
                    cvBadge.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                    cvBadgeText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    cvBadgeText.setText("CV: Cross");
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