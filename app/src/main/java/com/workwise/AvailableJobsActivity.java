package com.workwise;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.workwise.adapters.JobListAdapter;
import com.workwise.models.JobListingResponse;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableJobsActivity extends AppCompatActivity {

    private ImageButton backButton;
    private RecyclerView jobsRecyclerView;
    private ProgressBar loadingProgressBar;
    private MaterialCardView emptyStateCard;
    private JobListAdapter adapter;
    private apiService api;
    private Call<List<JobListingResponse>> activeCall;

    private static final int LIMIT = 20;
    private static final int INITIAL_OFFSET = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.available_jobs);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadAllJobs();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            Log.e("JOBS_API", "Retrofit init failed", e);
            Toast.makeText(this, "API initialization error", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new JobListAdapter(this, new ArrayList<>());
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void loadAllJobs() {
        if (api == null) {
            showEmptyState();
            return;
        }

        showLoadingState();
        activeCall = api.getAllJobs(apiConfig.tokenJobsAll, LIMIT, INITIAL_OFFSET);
        activeCall.enqueue(new Callback<List<JobListingResponse>>() {
            @Override
            public void onResponse(Call<List<JobListingResponse>> call, Response<List<JobListingResponse>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    showEmptyState();
                    return;
                }

                List<JobListingResponse> jobs = response.body();
                adapter.updateJobs(jobs);
                hideLoadingState();
            }

            @Override
            public void onFailure(Call<List<JobListingResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e("JOBS_API", "Failed to load jobs", t);
                showEmptyState();
                Toast.makeText(AvailableJobsActivity.this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadJobsByFilter(String employmentType, String workArrangement) {
        if (api == null) {
            showEmptyState();
            return;
        }

        showLoadingState();

        if (workArrangement != null && !workArrangement.isEmpty()) {
            activeCall = api.getFilteredJobs(apiConfig.tokenJobsFilter, employmentType, workArrangement, LIMIT, INITIAL_OFFSET);
        } else if (employmentType != null && !employmentType.isEmpty()) {
            activeCall = api.getJobsByType(apiConfig.tokenJobsFilter, employmentType, LIMIT, INITIAL_OFFSET);
        } else {
            loadAllJobs();
            return;
        }

        activeCall.enqueue(new Callback<List<JobListingResponse>>() {
            @Override
            public void onResponse(Call<List<JobListingResponse>> call, Response<List<JobListingResponse>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    showEmptyState();
                    return;
                }

                List<JobListingResponse> jobs = response.body();
                adapter.updateJobs(jobs);
                hideLoadingState();
            }

            @Override
            public void onFailure(Call<List<JobListingResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e("JOBS_API", "Failed to load filtered jobs", t);
                showEmptyState();
                Toast.makeText(AvailableJobsActivity.this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingState() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        jobsRecyclerView.setVisibility(View.GONE);
        emptyStateCard.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        loadingProgressBar.setVisibility(View.GONE);
        jobsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateCard.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        jobsRecyclerView.setVisibility(View.GONE);
        emptyStateCard.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeCall != null) {
            activeCall.cancel();
        }
    }
}
