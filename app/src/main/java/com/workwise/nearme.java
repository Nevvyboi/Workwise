package com.workwise;

import static java.lang.Math.E;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.workwise.jobs.jobapt;
import com.workwise.models.job;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.ui.bottomNav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class nearme extends bottomNav implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private Geocoder geocoder;
    // --- 1. REMOVED userLocality VARIABLE ---

    private ChipGroup filterGroup;
    private Chip chipAll, chipFull, chipPart, chipContract, chipRemote;
    private RecyclerView rvJobs;

    private jobapt jobsAdapter;
    private final List<job> allJobs = new ArrayList<>();
    private final List<job> visibleJobs = new ArrayList<>();

    private final Map<Integer, Float> jobDistances = new HashMap<>();

    private static final int JOB_LIMIT = 100; // Get up to 100 jobs
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // ... (rest of your variables are unchanged) ...
    private View loadingOverlay;
    private View pulseCircle;
    private View permissionPrompt;
    private MaterialButton allowLocationButton;
    private MaterialButton continueWithoutButton;
    private ObjectAnimator pulseAnimator;
    private boolean isMapReady = false;
    private boolean areJobsLoaded = false;
    private boolean permissionGranted = false;

    @Override
    protected String getCurrentNavItem() {
        return "nearme";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearme);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        initViews();
        setupRecycler();
        setupFilters();
        setupPermissionPrompt();
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
        loadingOverlay = findViewById(R.id.loadingOverlay);
        pulseCircle = findViewById(R.id.pulseCircle);
        permissionPrompt = findViewById(R.id.permissionPrompt);
        allowLocationButton = findViewById(R.id.allowLocationButton);
        continueWithoutButton = findViewById(R.id.continueWithoutButton);
    }

    private void setupRecycler() {
        jobsAdapter = new jobapt(visibleJobs, jobDistances, this);
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

    // ================== PERMISSION HANDLING ==================

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupPermissionPrompt() {
        if (allowLocationButton != null) {
            allowLocationButton.setOnClickListener(v -> {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Location access already enabled", Toast.LENGTH_SHORT).show();
                    hidePermissionPrompt();
                    startLoading();
                } else {
                    requestLocationPermission();
                }
            });
        }
        if (continueWithoutButton != null) {
            continueWithoutButton.setOnClickListener(v -> {
                hidePermissionPrompt();
                startLoadingWithoutLocation();
            });
        }
    }

    private void hidePermissionPrompt() {
        if (permissionPrompt != null) {
            permissionPrompt.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (permissionPrompt != null) {
                            permissionPrompt.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("WorkWise needs your location to show jobs near you and calculate distances. This helps you find the closest opportunities.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST);
                    })
                    .setNegativeButton("Not Now", (dialog, which) -> {
                        hidePermissionPrompt();
                        startLoadingWithoutLocation();
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
                Toast.makeText(this, "✓ Location access enabled", Toast.LENGTH_SHORT).show();
                hidePermissionPrompt();
                startLoading();
            } else {
                Toast.makeText(this, "Location permission denied. Showing all jobs.",
                        Toast.LENGTH_LONG).show();
                hidePermissionPrompt();
                startLoadingWithoutLocation();
            }
        }
    }

    // ================== LOADING INITIALIZATION ==================

    private void startLoading() {
        showLoading();
        setupMap();
        getUserLocation(); // This will get GPS coordinates
        loadJobsFromApi(); // This will load ALL jobs
    }

    private void startLoadingWithoutLocation() {
        showLoading();
        setupMap();
        loadJobsFromApi(); // This will load ALL jobs
    }

    private void setupMap() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    // ================== LOADING STATE ==================

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            startPulseAnimation();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            stopPulseAnimation();
            loadingOverlay.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        if (loadingOverlay != null) {
                            loadingOverlay.setVisibility(View.GONE);
                            loadingOverlay.setAlpha(1f);
                        }
                    })
                    .start();
        }
    }

    private void startPulseAnimation() {
        if (pulseCircle != null) {
            pulseAnimator = ObjectAnimator.ofFloat(pulseCircle, "scaleX", 1f, 1.3f, 1f);
            pulseAnimator.setDuration(1500);
            pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator scaleY = ObjectAnimator.ofFloat(pulseCircle, "scaleY", 1f, 1.3f, 1f);
            scaleY.setDuration(1500);
            scaleY.setRepeatCount(ObjectAnimator.INFINITE);
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            pulseAnimator.start();
            scaleY.start();
        }
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }

    private void checkAndHideLoading() {
        if (isMapReady && areJobsLoaded) {
            hideLoading();
        }
    }

    // ================== GET USER LOCATION ==================

    private void getUserLocation() {
        if (!checkLocationPermission()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = location;
                        updateMapWithUserLocation();

                        // Now that we have location, re-calculate distances
                        if (!allJobs.isEmpty()) {
                            calculateDistancesForAllJobs();
                            // Re-apply filter to sort by new distances
                            applyFilter(getSelectedFilterKey());
                        }

                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // *** THIS IS THE FIXED LINE ***
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================== MAP CALLBACK ==================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        if (checkLocationPermission() && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        LatLng southAfrica = new LatLng(-28.4793, 24.6727);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(southAfrica, 5f));

        if (userLocation != null) {
            updateMapWithUserLocation();
        }

        isMapReady = true;
        checkAndHideLoading();
    }

    private void updateMapWithUserLocation() {
        if (map != null && userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(),
                    userLocation.getLongitude());

            map.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
        }
    }

    // ================== API: LOAD JOBS ==================

    private void loadJobsFromApi() {
        apiService api = apiClient.get().create(apiService.class);

        // --- 3. REMOVED LOCATION FILTER ---
        // This call now fetches ALL jobs, not just jobs from 'userLocality'
        Call<List<job>> call = api.getActiveJobs(
                apiConfig.tokenJobsList,
                JOB_LIMIT,
                0,
                null, // Filters will be applied on the client side
                null,
                null  // <-- This is the important change
        );
        // --- END CHANGE ---

        call.enqueue(new Callback<List<job>>() {
            @Override
            public void onResponse(@NonNull Call<List<job>> call,
                                   @NonNull Response<List<job>> response) {
                areJobsLoaded = true;

                if (!response.isSuccessful()) {
                    checkAndHideLoading();
                    Toast.makeText(nearme.this, "Failed to load jobs: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    checkAndHideLoading();
                    Toast.makeText(nearme.this, "No jobs available", Toast.LENGTH_SHORT).show();
                    return;
                }

                allJobs.clear();
                allJobs.addAll(response.body());

                // We have the jobs, now calculate distances if we have location
                if (userLocation != null) {
                    calculateDistancesForAllJobs();
                }

                // Apply default filter ("ALL") which will also sort by distance
                applyFilter("ALL");

                Toast.makeText(nearme.this, allJobs.size() + " jobs loaded", Toast.LENGTH_SHORT).show();
                checkAndHideLoading();
            }

            @Override
            public void onFailure(@NonNull Call<List<job>> call, @NonNull Throwable t) {
                areJobsLoaded = true;
                checkAndHideLoading();
                Toast.makeText(nearme.this, "Error loading jobs: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================== DISTANCE CALCULATION ==================

    private void calculateDistancesForAllJobs() {
        if (userLocation == null) return;

        for (job j : allJobs) {
            if (j == null || j.getJobLocation() == null) continue;

            // Get coordinates for job location (e.g., "Sandton, Gauteng")
            LatLng jobLatLng = getCoordinatesFromAddress(j.getJobLocation());

            if (jobLatLng != null) {
                float distance = calculateDistance(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        jobLatLng.latitude,
                        jobLatLng.longitude
                );
                jobDistances.put(j.getJobId(), distance);
            }
        }

        if (jobsAdapter != null) {
            jobsAdapter.notifyDataSetChanged();
        }
    }

    private LatLng getCoordinatesFromAddress(String address) {
        try {
            // Geocoder is not perfect, but it's good for city/suburb names
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            // Log the error but don't crash
            Log.e("NearMe", "Geocoder failed for address: " + address, e);
        }
        return null; // Return null if address can't be found
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        // Convert meters to kilometers
        return results[0] / 1000f;
    }

    // ================== FILTERING ==================

    private String getSelectedFilterKey() {
        int checkedId = filterGroup.getCheckedChipId();
        if (checkedId == R.id.chipFull) return "FULL";
        if (checkedId == R.id.chipPart) return "PART";
        if (checkedId == R.id.chipContract) return "CONTRACT";
        if (checkedId == R.id.chipRemote) return "REMOTE";
        return "ALL";
    }

    private void applyFilter(String key) {
        visibleJobs.clear();

        for (job j : allJobs) {
            if (j == null) continue;

            String type = j.getEmploymentType() != null
                    ? j.getEmploymentType().toLowerCase()
                    : "";
            String workType = j.getWorkArrangement() != null
                    ? j.getWorkArrangement().toLowerCase()
                    : "";

            boolean shouldAdd = false;

            switch (key) {
                case "FULL":
                    shouldAdd = type.contains("full");
                    break;
                case "PART":
                    shouldAdd = type.contains("part");
                    break;
                case "CONTRACT":
                    shouldAdd = type.contains("contract");
                    break;
                case "REMOTE":
                    shouldAdd = workType.contains("remote");
                    break;
                case "ALL":
                default:
                    shouldAdd = true;
                    break;
            }

            if (shouldAdd) {
                visibleJobs.add(j);
            }
        }

        // --- 4. THIS SORTING IS NOW THE MOST IMPORTANT PART ---
        // If we have location, sort the list by distance
        if (userLocation != null && !jobDistances.isEmpty()) {
            Collections.sort(visibleJobs, new Comparator<job>() {
                @Override
                public int compare(job j1, job j2) {
                    Float dist1 = jobDistances.get(j1.getJobId());
                    Float dist2 = jobDistances.get(j2.getJobId());

                    // This logic puts jobs with unknown locations (dist1/2 == null)
                    // at the bottom of the list.
                    if (dist1 == null && dist2 == null) return 0;
                    if (dist1 == null) return 1;
                    if (dist2 == null) return -1;

                    return Float.compare(dist1, dist2); // Sorts closest first
                }
            });
        }
        // If we don't have location, the list remains sorted by date (from API)

        jobsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPulseAnimation();
    }
}