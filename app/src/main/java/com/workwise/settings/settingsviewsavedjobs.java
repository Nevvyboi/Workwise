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
import androidx.core.view.WindowCompat;

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
        // ... (unchanged) ...
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.settingsviewsavedjobs);

        prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // ... (unchanged) ...
        backButton = findViewById(R.id.backButton);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        savedJobsContainer = findViewById(R.id.savedJobsContainer);
    }

    private void setupClickListeners() {
        // ... (unchanged) ...
        if (backButton != null) {
            backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void loadSavedJobs() {
        // ... (previous null checks are still here) ...
        if (emptyStateCard != null) {
            emptyStateCard.setVisibility(View.GONE);
        }
        if (savedJobsContainer != null) {
            savedJobsContainer.setVisibility(View.GONE);
        }


        apiService api = null; // Initialize as null
        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (api == null) {
            Toast.makeText(this, "Error initializing API. Please restart.", Toast.LENGTH_LONG).show();
            showEmptyState(); // Show the empty state as we can't load
            return;
        }


        Call<List<savedJobs>> call = api.getSavedJobs(userId, apiConfig.tokenSavedList);

        call.enqueue(new Callback<List<savedJobs>>() {
            @Override
            public void onResponse(@NonNull Call<List<savedJobs>> call,
                                   @NonNull Response<List<savedJobs>> response) {
                // ... (rest of method unchanged) ...
                if (isFinishing() || isDestroyed()) return;
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
                // ... (rest of method unchanged) ...
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(settingsviewsavedjobs.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        // ... (unchanged) ...
        if (emptyStateCard != null) {
            emptyStateCard.setVisibility(View.VISIBLE);
        }
        if (savedJobsContainer != null) {
            savedJobsContainer.setVisibility(View.GONE);
        }
    }

    private void populateSavedJobs(List<savedJobs> jobs) {
        // ... (unchanged) ...
        if (savedJobsContainer == null) return;
        savedJobsContainer.removeAllViews();
        if (emptyStateCard != null) {
            emptyStateCard.setVisibility(View.GONE);
        }
        savedJobsContainer.setVisibility(View.VISIBLE);
        for (savedJobs job : jobs) {
            View jobCard = createJobCard(job);
            savedJobsContainer.addView(jobCard);
        }
    }

    private View createJobCard(savedJobs job) {
        LayoutInflater inflater = LayoutInflater.from(this);
        MaterialCardView cardView = (MaterialCardView) inflater.inflate(
                R.layout.itemsavedjob, savedJobsContainer, false);

        // Find views
        TextView tvJobTitle = cardView.findViewById(R.id.tv_saved_job_title);
        TextView tvCompanyName = cardView.findViewById(R.id.tv_saved_company_name);
        TextView tvLocationInfo = cardView.findViewById(R.id.tv_saved_location_info);
        TextView tvSalary = cardView.findViewById(R.id.tv_saved_salary);
        TextView tvSavedDate = cardView.findViewById(R.id.tv_saved_date);
        MaterialButton btnView = cardView.findViewById(R.id.btn_view_saved_job);
        MaterialButton btnRemove = cardView.findViewById(R.id.btn_remove_saved_job);

        // --- START FIX: Add null checks for all views ---
        if (tvJobTitle != null) {
            tvJobTitle.setText(job.getJobTitle());
        }

        if (tvCompanyName != null) {
            tvCompanyName.setText(job.getCompanyName());
        }

        if (tvLocationInfo != null) {
            String locationInfo = job.getJobLocation() != null ? job.getJobLocation() : "Location not specified";
            tvLocationInfo.setText(locationInfo);
        }

        if (tvSalary != null) {
            String salary = job.getSalaryRange() != null ? job.getSalaryRange() : "Salary not disclosed";
            tvSalary.setText(salary);
        }

        if (tvSavedDate != null) {
            String savedDate = job.getSavedAt() != null ? "Saved: " + formatDate(job.getSavedAt()) : "Recently saved";
            tvSavedDate.setText(savedDate);
        }

        if (btnView != null) {
            btnView.setOnClickListener(v -> {
                Toast.makeText(this, "View job details: " + job.getJobTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRemove != null) {
            btnRemove.setOnClickListener(v -> {
                removeSavedJob(job, cardView);
            });
        }
        // --- END FIX ---

        return cardView;
    }

    private void removeSavedJob(savedJobs job, View cardView) {

        // --- START FIX: Add try-catch for API initialization ---
        apiService api;
        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: API not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (api == null) {
            Toast.makeText(this, "Error: API not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- END FIX ---

        Call<apiResponse> call = api.deleteSavedJob(
                userId,
                job.getSavedJobId(),
                apiConfig.tokenSavedDelete
        );

        call.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(@NonNull Call<apiResponse> call,
                                   @NonNull Response<apiResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    if (savedJobsContainer != null) {
                        savedJobsContainer.removeView(cardView);
                        if (savedJobsContainer.getChildCount() == 0) {
                            showEmptyState();
                        }
                    }
                    Toast.makeText(settingsviewsavedjobs.this,
                            "Job removed from saved",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(settingsviewsavedjobs.this,
                            "Failed to remove job",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<apiResponse> call,
                                  @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(settingsviewsavedjobs.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String isoDate) {
        // ... (unchanged) ...
        if (isoDate == null || isoDate.isEmpty()) {
            return "Recently";
        }
        try {
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
        loadSavedJobs();
    }
}