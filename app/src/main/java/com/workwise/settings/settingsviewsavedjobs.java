package com.workwise.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;
import com.workwise.models.savedJobs;
import com.workwise.models.apiResponse;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class settingsviewsavedjobs extends AppCompatActivity {

    private ImageButton backButton;
    private MaterialCardView emptyStateCard;
    private LinearLayout savedJobsContainer;
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsviewsavedjobs);

        // Get user ID from SharedPreferences
        prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        // Show loading state
        emptyStateCard.setVisibility(View.GONE);
        savedJobsContainer.setVisibility(View.GONE);

        apiService api = apiClient.get().create(apiService.class);
        Call<List<savedJobs>> call = api.getSavedJobs(userId, apiConfig.tokenSavedList);

        call.enqueue(new Callback<List<savedJobs>>() {
            @Override
            public void onResponse(@NonNull Call<List<savedJobs>> call,
                                   @NonNull Response<List<savedJobs>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(settingsviewsavedjobs.this,
                            "Failed to load saved jobs: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                    return;
                }

                List<savedJobs> jobs = response.body();
                if (jobs == null || jobs.isEmpty()) {
                    showEmptyState();
                } else {
                    populateSavedJobs(jobs);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<savedJobs>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(settingsviewsavedjobs.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        emptyStateCard.setVisibility(View.VISIBLE);
        savedJobsContainer.setVisibility(View.GONE);
    }

    private void populateSavedJobs(List<savedJobs> jobs) {
        // Clear existing views
        savedJobsContainer.removeAllViews();

        // Hide empty state
        emptyStateCard.setVisibility(View.GONE);
        savedJobsContainer.setVisibility(View.VISIBLE);

        // Add job cards
        for (savedJobs job : jobs) {
            View jobCard = createJobCard(job);
            savedJobsContainer.addView(jobCard);
        }
    }

    private View createJobCard(savedJobs job) {
        LayoutInflater inflater = LayoutInflater.from(this);
        MaterialCardView cardView = (MaterialCardView) inflater.inflate(
                R.layout.itemsavedjob, savedJobsContainer, false);

        // Get views from card
        TextView tvJobTitle = cardView.findViewById(R.id.tv_saved_job_title);
        TextView tvCompanyName = cardView.findViewById(R.id.tv_saved_company_name);
        TextView tvLocationInfo = cardView.findViewById(R.id.tv_saved_location_info);
        TextView tvSalary = cardView.findViewById(R.id.tv_saved_salary);
        TextView tvSavedDate = cardView.findViewById(R.id.tv_saved_date);
        MaterialButton btnView = cardView.findViewById(R.id.btn_view_saved_job);
        MaterialButton btnRemove = cardView.findViewById(R.id.btn_remove_saved_job);

        // Set data
        tvJobTitle.setText(job.getJobTitle());
        tvCompanyName.setText(job.getCompanyName());

        String locationInfo = job.getJobLocation() != null ? job.getJobLocation() : "Location not specified";
        tvLocationInfo.setText(locationInfo);

        String salary = job.getSalaryRange() != null ? job.getSalaryRange() : "Salary not disclosed";
        tvSalary.setText(salary);

        String savedDate = job.getSavedAt() != null ? "Saved: " + formatDate(job.getSavedAt()) : "Recently saved";
        tvSavedDate.setText(savedDate);

        // Set click listeners
        btnView.setOnClickListener(v -> {
            // TODO: Open job details activity
            Toast.makeText(this, "View job details: " + job.getJobTitle(), Toast.LENGTH_SHORT).show();
        });

        btnRemove.setOnClickListener(v -> {
            removeSavedJob(job, cardView);
        });

        return cardView;
    }

    private void removeSavedJob(savedJobs job, View cardView) {
        apiService api = apiClient.get().create(apiService.class);
        Call<apiResponse> call = api.deleteSavedJob(
                userId,
                job.getSavedJobId(),
                apiConfig.tokenSavedDelete
        );

        call.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(@NonNull Call<apiResponse> call,
                                   @NonNull Response<apiResponse> response) {
                if (response.isSuccessful()) {
                    // Remove card from view with animation
                    savedJobsContainer.removeView(cardView);

                    Toast.makeText(settingsviewsavedjobs.this,
                            "Job removed from saved",
                            Toast.LENGTH_SHORT).show();

                    // Check if container is now empty
                    if (savedJobsContainer.getChildCount() == 0) {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(settingsviewsavedjobs.this,
                            "Failed to remove job",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<apiResponse> call,
                                  @NonNull Throwable t) {
                Toast.makeText(settingsviewsavedjobs.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String isoDate) {
        // Simple date formatting - you can enhance this
        if (isoDate == null || isoDate.isEmpty()) {
            return "Recently";
        }

        try {
            // Extract just the date part (YYYY-MM-DD)
            if (isoDate.contains("T")) {
                String datePart = isoDate.split("T")[0];
                String[] parts = datePart.split("-");
                if (parts.length == 3) {
                    return parts[2] + "/" + parts[1] + "/" + parts[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Recently";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh saved jobs when activity resumes
        loadSavedJobs();
    }
}