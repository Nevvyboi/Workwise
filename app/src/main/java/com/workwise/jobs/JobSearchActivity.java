package com.workwise.jobs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.workwise.R;
import com.workwise.models.job;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView jobsRecycler;
    private jobapt jobsAdapter;
    private ProgressBar searchProgress;
    private LinearLayout emptyState;
    private TextView emptyText;
    private ImageButton backButton;

    private apiService api;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<List<job>> currentApiCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_search);

        // Initialize API
        api = apiClient.get().create(apiService.class);

        // Find Views
        searchInput = findViewById(R.id.searchInput);
        jobsRecycler = findViewById(R.id.jobsRecycler);
        searchProgress = findViewById(R.id.searchProgress);
        emptyState = findViewById(R.id.emptyState);
        emptyText = findViewById(R.id.emptyText);
        backButton = findViewById(R.id.backButton);

        // Setup RecyclerView
        jobsRecycler.setLayoutManager(new LinearLayoutManager(this));
        // Pass empty distance map, as search doesn't sort by distance
        jobsAdapter = new jobapt(new ArrayList<>(), new HashMap<>(), this);
        jobsRecycler.setAdapter(jobsAdapter);

        // Setup Listeners
        backButton.setOnClickListener(v -> finish());
        setupSearchInput();

        // Show initial empty state
        showEmptyState("Start typing to find jobs");
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous delayed search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                // Cancel ongoing API call
                if (currentApiCall != null) {
                    currentApiCall.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() < 2) {
                    // Don't search for less than 2 chars
                    showEmptyState("Start typing to find jobs");
                    jobsAdapter.updateJobs(new ArrayList<>());
                    searchProgress.setVisibility(View.GONE);
                    return;
                }

                // Show loading spinner
                searchProgress.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                jobsRecycler.setVisibility(View.GONE);

                // Create a delayed search runnable
                searchRunnable = () -> performSearch(query);

                // Delay search by 500ms to avoid API calls on every keystroke
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        // Also handle "Search" button on keyboard
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                performSearch(v.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        searchProgress.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        jobsRecycler.setVisibility(View.GONE);

        currentApiCall = api.searchJobs(
                apiConfig.tokenJobSearch, // Use your search token
                query,
                null, // No filter for now
                null, // No filter for now
                20,   // Limit
                0     // Offset
        );

        currentApiCall.enqueue(new Callback<List<job>>() {
            @Override
            public void onResponse(@NonNull Call<List<job>> call, @NonNull Response<List<job>> response) {
                if (isFinishing()) return; // Activity closed
                searchProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        showEmptyState("No jobs found for '" + query + "'");
                    } else {
                        showResults(response.body());
                    }
                } else {
                    showEmptyState("Error loading results");
                    Toast.makeText(JobSearchActivity.this, "Failed to search: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<job>> call, @NonNull Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                searchProgress.setVisibility(View.GONE);
                showEmptyState("Network error");
                Toast.makeText(JobSearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState(String message) {
        jobsRecycler.setVisibility(View.GONE);
        searchProgress.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }

    private void showResults(List<job> jobs) {
        emptyState.setVisibility(View.GONE);
        searchProgress.setVisibility(View.GONE);
        jobsRecycler.setVisibility(View.VISIBLE);
        jobsAdapter.updateJobs(jobs);
    }
}