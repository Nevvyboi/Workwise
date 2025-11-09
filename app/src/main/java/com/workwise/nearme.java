package com.workwise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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

    private ChipGroup filterGroup;
    private Chip chipAll, chipFull, chipPart, chipContract, chipRemote;
    private RecyclerView rvJobs;

    private jobapt jobsAdapter;
    private final List<job> allJobs = new ArrayList<>();
    private final List<job> visibleJobs = new ArrayList<>();

    // Store calculated distances for each job
    private final Map<Integer, Float> jobDistances = new HashMap<>();

    private static final int JOB_LIMIT = 30;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    protected String getCurrentNavItem() {
        return "nearme";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearme);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        initViews();
        setupMap();
        setupRecycler();
        setupFilters();
        requestLocationPermission();
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
        // IMPORTANT: Pass context to adapter for bookmark functionality
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

    // ================== LOCATION PERMISSION ==================

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================== GET USER LOCATION ==================

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = location;
                        updateMapWithUserLocation();
                        calculateDistancesForAllJobs();
                    } else {
                        Toast.makeText(this, "Unable to get current location",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting location: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ================== MAP CALLBACK ==================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        // Enable location layer if permission granted
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        // Default view centered on South Africa
        LatLng southAfrica = new LatLng(-28.4793, 24.6727);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(southAfrica, 5f));

        // If we already have user location, update the map
        if (userLocation != null) {
            updateMapWithUserLocation();
        }
    }

    private void updateMapWithUserLocation() {
        if (map != null && userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(),
                    userLocation.getLongitude());

            // Add marker for user location
            map.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            // Move camera to user location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
        }
    }

    // ================== API: LOAD JOBS ==================

    private void loadJobsFromApi() {
        apiService api = apiClient.get().create(apiService.class);
        Call<List<job>> call = api.getJobs(apiConfig.tokenJobsList, JOB_LIMIT);

        call.enqueue(new Callback<List<job>>() {
            @Override
            public void onResponse(@NonNull Call<List<job>> call,
                                   @NonNull Response<List<job>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(nearme.this,
                            "Failed to load jobs: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(nearme.this,
                            "No jobs available", Toast.LENGTH_SHORT).show();
                    return;
                }

                allJobs.clear();
                allJobs.addAll(response.body());

                // Calculate distances if we have user location
                if (userLocation != null) {
                    calculateDistancesForAllJobs();
                }

                applyFilter("ALL");

                Toast.makeText(nearme.this,
                        allJobs.size() + " jobs loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<job>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(nearme.this,
                        "Error loading jobs: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================== DISTANCE CALCULATION ==================

    private void calculateDistancesForAllJobs() {
        if (userLocation == null) return;

        for (job j : allJobs) {
            if (j == null || j.getJobLocation() == null) continue;

            // Get coordinates for job location
            LatLng jobLatLng = getCoordinatesFromAddress(j.getJobLocation());

            if (jobLatLng != null) {
                // Calculate distance
                float distance = calculateDistance(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        jobLatLng.latitude,
                        jobLatLng.longitude
                );

                jobDistances.put(j.getJobId(), distance);
            }
        }

        // Notify adapter to update distances
        if (jobsAdapter != null) {
            jobsAdapter.notifyDataSetChanged();
        }
    }

    private LatLng getCoordinatesFromAddress(String address) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        // Convert meters to kilometers
        return results[0] / 1000f;
    }

    // ================== FILTERING ==================

    private void applyFilter(String key) {
        visibleJobs.clear();

        for (job j : allJobs) {
            if (j == null) continue;

            String type = j.getEmploymentType() != null
                    ? j.getEmploymentType().toLowerCase()
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
                    String loc = j.getJobLocation() != null
                            ? j.getJobLocation().toLowerCase()
                            : "";
                    shouldAdd = type.contains("remote") || loc.contains("remote");
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

        // Sort by distance if we have user location
        if (userLocation != null && !jobDistances.isEmpty()) {
            Collections.sort(visibleJobs, new Comparator<job>() {
                @Override
                public int compare(job j1, job j2) {
                    Float dist1 = jobDistances.get(j1.getJobId());
                    Float dist2 = jobDistances.get(j2.getJobId());

                    if (dist1 == null && dist2 == null) return 0;
                    if (dist1 == null) return 1;
                    if (dist2 == null) return -1;

                    return Float.compare(dist1, dist2);
                }
            });
        }

        jobsAdapter.notifyDataSetChanged();
    }
}