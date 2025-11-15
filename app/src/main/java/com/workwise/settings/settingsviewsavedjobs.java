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
import com.workwise.models.apiResponse;
import com.workwise.models.savedJobs;
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
    private int userId;
    private Call<List<savedJobs>> activeLoadCall;
    private Call<apiResponse> activeDeleteCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.settingsviewsavedjobs);

        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadSavedJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeLoadCall != null && !activeLoadCall.isCanceled()) {
            activeLoadCall.cancel();
            activeLoadCall = null;
        }
        if (activeDeleteCall != null && !activeDeleteCall.isCanceled()) {
            activeDeleteCall.cancel();
            activeDeleteCall = null;
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        savedJobsContainer = findViewById(R.id.savedJobsContainer);
    }

    private void setupClickListeners() {
        if (backButton != null) backButton.setOnClickListener(v -> finish());
    }

    private void loadSavedJobs() {
        if (activeLoadCall != null && !activeLoadCall.isCanceled()) {
            activeLoadCall.cancel();
        }

        if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
        if (savedJobsContainer != null) savedJobsContainer.setVisibility(View.GONE);

        apiService api = safeApi();
        if (api == null) {
            Toast.makeText(this, "API service unavailable. Please restart.", Toast.LENGTH_LONG).show();
            showEmptyState();
            return;
        }

        activeLoadCall = api.getSavedJobs(userId, apiConfig.tokenSavedList);
        activeLoadCall.enqueue(new Callback<List<savedJobs>>() {
            @Override
            public void onResponse(@NonNull Call<List<savedJobs>> call, @NonNull Response<List<savedJobs>> response) {
                if (isFinishing() || isDestroyed() || activeLoadCall == null) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(settingsviewsavedjobs.this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
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
            public void onFailure(@NonNull Call<List<savedJobs>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || activeLoadCall == null) return;
                android.util.Log.e("SAVED_JOBS", "Error loading saved jobs", t);
                showEmptyState();
            }
        });
    }

    private apiService safeApi() {
        try {
            return apiClient.get().create(apiService.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void showEmptyState() {
        if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
        if (savedJobsContainer != null) savedJobsContainer.setVisibility(View.GONE);
    }

    private void populateSavedJobs(List<savedJobs> jobs) {
        if (savedJobsContainer == null) return;
        savedJobsContainer.removeAllViews();
        if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
        savedJobsContainer.setVisibility(View.VISIBLE);

        for (savedJobs job : jobs) {
            View card = createJobCard(job);
            if (card != null) savedJobsContainer.addView(card);
        }
    }

    private View createJobCard(savedJobs job) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(
                    R.layout.itemsavedjob, savedJobsContainer, false);

            TextView tvJobTitle = cardView.findViewById(R.id.tv_saved_job_title);
            TextView tvCompanyName = cardView.findViewById(R.id.tv_saved_company_name);
            TextView tvLocationInfo = cardView.findViewById(R.id.tv_saved_location_info);
            TextView tvSalary = cardView.findViewById(R.id.tv_saved_salary);
            TextView tvSavedDate = cardView.findViewById(R.id.tv_saved_date);
            MaterialButton btnView = cardView.findViewById(R.id.btn_view_saved_job);
            MaterialButton btnRemove = cardView.findViewById(R.id.btn_remove_saved_job);

            if (tvJobTitle != null) tvJobTitle.setText(job.getJobTitle());
            if (tvCompanyName != null) tvCompanyName.setText(job.getCompanyName());
            if (tvLocationInfo != null)
                tvLocationInfo.setText(job.getJobLocation() != null ? job.getJobLocation() : "Location not specified");
            if (tvSalary != null)
                tvSalary.setText(job.getSalaryRange() != null ? job.getSalaryRange() : "Salary not disclosed");
            if (tvSavedDate != null)
                tvSavedDate.setText(job.getSavedAt() != null ? "Saved: " + formatDate(job.getSavedAt()) : "Recently saved");

            if (btnView != null) {
                btnView.setOnClickListener(v ->
                        Toast.makeText(this, "View: " + job.getJobTitle(), Toast.LENGTH_SHORT).show());
            }
            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> removeSavedJob(job, cardView));
            }
            return cardView;
        } catch (Exception e) {
            return null;
        }
    }

    private void removeSavedJob(savedJobs job, View cardView) {
        apiService api = safeApi();
        if (api == null) {
            Toast.makeText(this, "API error", Toast.LENGTH_SHORT).show();
            return;
        }
        activeDeleteCall = api.deleteSavedJob(userId, job.getSavedJobId(), apiConfig.tokenSavedDelete);
        activeDeleteCall.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(@NonNull Call<apiResponse> call,
                                   @NonNull Response<apiResponse> response) {
                if (isFinishing() || isDestroyed() || activeDeleteCall == null) return;
                if (!response.isSuccessful()) {
                    Toast.makeText(settingsviewsavedjobs.this, "Remove failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (savedJobsContainer != null) {
                    savedJobsContainer.removeView(cardView);
                    if (savedJobsContainer.getChildCount() == 0) showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<apiResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || activeDeleteCall == null) return;
                Toast.makeText(settingsviewsavedjobs.this, "Error removing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Recently";
        try {
            if (isoDate.contains("T")) {
                String datePart = isoDate.split("T")[0];
                String[] p = datePart.split("-");
                if (p.length == 3) return p[2] + "/" + p[1] + "/" + p[0];
            }
        } catch (Exception ignored) {}
        return "Recently";
    }
}
