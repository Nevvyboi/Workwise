package com.workwise;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.workwise.jobs.jobapt;
import com.workwise.models.job;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.ui.bottomNav;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class nearme extends bottomNav implements OnMapReadyCallback {

    private GoogleMap map;

    private ChipGroup filterGroup;
    private Chip chipAll, chipFull, chipPart, chipContract, chipRemote;
    private RecyclerView rvJobs;

    private jobapt jobsAdapter;
    private final List<job> allJobs = new ArrayList<>();
    private final List<job> visibleJobs = new ArrayList<>();

    private static final int JOB_LIMIT = 30; // how many random jobs to fetch

    @Override
    protected String getCurrentNavItem() {
        return "nearme";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearme);

        initViews();
        setupMap();
        setupRecycler();
        setupFilters();
        loadJobsFromApi();
    }

    // ================== INIT ==================

    private void initViews() {
        filterGroup = findViewById(R.id.filterGroup);
        chipAll = findViewById(R.id.chipAll);
        chipFull = findViewById(R.id.chipFull);
        chipPart = findViewById(R.id.chipPart);
        chipContract = findViewById(R.id.chipContract);
        chipRemote = findViewById(R.id.chipRemote);
        rvJobs = findViewById(R.id.rvJobs);
    }

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupRecycler() {
        jobsAdapter = new jobapt(visibleJobs);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(jobsAdapter);
        rvJobs.setNestedScrollingEnabled(false);
    }

    private void setupFilters() {
        chipAll.setChecked(true);

        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                applyFilter("ALL");
                return;
            }

            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                applyFilter("ALL");
            } else if (id == R.id.chipFull) {
                applyFilter("FULL");
            } else if (id == R.id.chipPart) {
                applyFilter("PART");
            } else if (id == R.id.chipContract) {
                applyFilter("CONTRACT");
            } else if (id == R.id.chipRemote) {
                applyFilter("REMOTE");
            }
        });
    }

    // ================== MAP CALLBACK ==================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        // Static default view (no GPS, no runtime permissions)
        LatLng southAfrica = new LatLng(-28.4793, 24.6727);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(southAfrica, 5f));
    }

    // ================== API: LOAD JOBS ==================

    private void loadJobsFromApi() {
        apiService api = apiClient.get().create(apiService.class);

        // Ensure you have in apiService:
        // @GET("v1/workwise/jobs")
        // Call<List<job>> getJobs(@Header("X-Endpoint-Token") String token,
        //                         @Query("limit") int limit);
        //
        // And in apiConfig:
        // public static final String tokenJobsList = "JOBLISTTOK111";
        //
        Call<List<job>> call = api.getJobs(apiConfig.tokenSavedList, JOB_LIMIT);

        call.enqueue(new Callback<List<job>>() {
            @Override
            public void onResponse(@NonNull Call<List<job>> call,
                                   @NonNull Response<List<job>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(nearme.this,
                            "Could not load jobs", Toast.LENGTH_SHORT).show();
                    return;
                }

                allJobs.clear();
                allJobs.addAll(response.body());
                applyFilter("ALL");
            }

            @Override
            public void onFailure(@NonNull Call<List<job>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(nearme.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== FILTERING ==================

    private void applyFilter(String key) {
        visibleJobs.clear();

        for (job j : allJobs) {
            if (j == null) continue;

            String type = j.getEmploymentType() != null
                    ? j.getEmploymentType().toLowerCase()
                    : "";

            switch (key) {
                case "FULL":
                    if (type.contains("full")) visibleJobs.add(j);
                    break;
                case "PART":
                    if (type.contains("part")) visibleJobs.add(j);
                    break;
                case "CONTRACT":
                    if (type.contains("contract")) visibleJobs.add(j);
                    break;
                case "REMOTE":
                    // treat remote if type mentions remote OR location mentions remote
                    String loc = j.getJobLocation() != null
                            ? j.getJobLocation().toLowerCase()
                            : "";
                    if (type.contains("remote") || loc.contains("remote")) {
                        visibleJobs.add(j);
                    }
                    break;
                case "ALL":
                default:
                    visibleJobs.add(j);
                    break;
            }
        }

        jobsAdapter.notifyDataSetChanged();
    }
}
