package com.workwise;

import android.Manifest;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.*;

public class nearme extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocation;

    private MaterialButton btnEnableGps;
    private ChipGroup filterGroup;
    private Chip chipAll, chipFull, chipPart, chipContract, chipRemote;
    private TextView resultsCount;
    private RecyclerView jobList;

    private JobAdapter adapter;
    private final List<Job> allJobs = new ArrayList<>();
    private final List<Job> visibleJobs = new ArrayList<>();

    // Permissions launcher
    private final ActivityResultLauncher<String[]> requestLocation =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                    enableMyLocation();
                    moveCameraToMyLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearme);
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupRecycler();
        seedDemoData();
        setupChips();
        updateList(JobType.ALL);

        // Map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Enable GPS button
        btnEnableGps.setOnClickListener(v -> openLocationSettings());
    }

    private void bindViews() {
        btnEnableGps = findViewById(R.id.btn_enable_gps);
        filterGroup = findViewById(R.id.filter_group);
        chipAll     = findViewById(R.id.chip_all);
        chipFull    = findViewById(R.id.chip_full_time);
        chipPart    = findViewById(R.id.chip_part_time);
        chipContract= findViewById(R.id.chip_contract);
        chipRemote  = findViewById(R.id.chip_remote);
        resultsCount= findViewById(R.id.resultsCount);
        jobList     = findViewById(R.id.job_list);
    }

    private void setupRecycler() {
        adapter = new JobAdapter(visibleJobs, job -> {
            // TODO: open job details
            Toast.makeText(this, "Open: " + job.title, Toast.LENGTH_SHORT).show();
        });
        jobList.setLayoutManager(new LinearLayoutManager(this));
        jobList.setAdapter(adapter);
    }

    private void setupChips() {
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            JobType type = JobType.ALL;
            if (chipFull.isChecked())      type = JobType.FULL_TIME;
            else if (chipPart.isChecked()) type = JobType.PART_TIME;
            else if (chipContract.isChecked()) type = JobType.CONTRACT;
            else if (chipRemote.isChecked())   type = JobType.REMOTE;
            updateList(type);
            updateMapMarkers(type);
        });
    }

    private void updateList(JobType filter) {
        visibleJobs.clear();
        for (Job j : allJobs) {
            if (filter == JobType.ALL || j.type == filter) visibleJobs.add(j);
        }
        resultsCount.setText(visibleJobs.size() + " jobs found nearby");
        adapter.notifyDataSetChanged();
    }

    private void seedDemoData() {
        // Replace with API data
        allJobs.add(new Job("Android Engineer", -25.7479, 28.2293, JobType.FULL_TIME, "Pretoria"));
        allJobs.add(new Job("Data Analyst (Remote)", -25.7550, 28.2050, JobType.REMOTE, "Remote / PTA"));
        allJobs.add(new Job("UX Designer (Contract)", -25.7400, 28.2200, JobType.CONTRACT, "Menlyn"));
        allJobs.add(new Job("Support Agent (Part-time)", -25.7300, 28.2300, JobType.PART_TIME, "Hatfield"));
        allJobs.add(new Job("Backend Engineer", -25.7700, 28.2000, JobType.FULL_TIME, "Brooklyn"));
    }

    // === Map ===
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        enableMyLocation();
        moveCameraToMyLocation();
        updateMapMarkers(JobType.ALL);
    }

    private void enableMyLocation() {
        if (map == null) return;
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            requestLocation.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        map.setMyLocationEnabled(true);
    }

    private void moveCameraToMyLocation() {
        fusedLocation.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null && map != null) {
                LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 12f));
            } else {
                // Fallback to Pretoria CBD if no fix yet
                LatLng pta = new LatLng(-25.7479, 28.2293);
                if (map != null) map.moveCamera(CameraUpdateFactory.newLatLngZoom(pta, 12f));
            }
        });
    }

    private void updateMapMarkers(JobType filter) {
        if (map == null) return;
        map.clear();
        for (Job j : allJobs) {
            if (filter != JobType.ALL && j.type != filter) continue;
            MarkerOptions mo = new MarkerOptions()
                    .position(new LatLng(j.lat, j.lng))
                    .title(j.title)
                    .snippet(j.area)
                    .icon(getMarkerIconFor(j.type));
            map.addMarker(mo);
        }
    }

    private BitmapDescriptor getMarkerIconFor(JobType type) {
        float hue = BitmapDescriptorFactory.HUE_AZURE; // default
        switch (type) {
            case FULL_TIME:  hue = BitmapDescriptorFactory.HUE_BLUE; break;
            case PART_TIME:  hue = BitmapDescriptorFactory.HUE_ORANGE; break;
            case CONTRACT:   hue = BitmapDescriptorFactory.HUE_GREEN; break;
            case REMOTE:     hue = BitmapDescriptorFactory.HUE_ROSE; break;
            case ALL: default: hue = BitmapDescriptorFactory.HUE_AZURE;
        }
        return BitmapDescriptorFactory.defaultMarker(hue);
    }

    private void openLocationSettings() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsOn = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsOn) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            Toast.makeText(this, "GPS already enabled", Toast.LENGTH_SHORT).show();
            enableMyLocation();
            moveCameraToMyLocation();
        }
    }

    // === Models & Adapter ===
    enum JobType { ALL, FULL_TIME, PART_TIME, CONTRACT, REMOTE }

    static class Job {
        final String title;
        final double lat, lng;
        final JobType type;
        final String area;
        Job(String title, double lat, double lng, JobType type, String area) {
            this.title = title; this.lat = lat; this.lng = lng; this.type = type; this.area = area;
        }
    }
}
