package com.workwise.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.workwise.R;

public class settingsmanagecv extends AppCompatActivity {

    private ImageButton backButton;
    private TextView cvStatusText;
    private TextView cvFileName;
    private TextView cvUploadDate;
    private MaterialButton uploadCvButton;
    private MaterialButton viewCvButton;
    private MaterialButton downloadCvButton;
    private MaterialButton removeCvButton;

    private boolean hasCv = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsmanagecv);

        initializeViews();
        setupClickListeners();
        updateCvStatus();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        cvStatusText = findViewById(R.id.cvStatusText);
        cvFileName = findViewById(R.id.cvFileName);
        cvUploadDate = findViewById(R.id.cvUploadDate);
        uploadCvButton = findViewById(R.id.uploadCvButton);
        viewCvButton = findViewById(R.id.viewCvButton);
        downloadCvButton = findViewById(R.id.downloadCvButton);
        removeCvButton = findViewById(R.id.removeCvButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        uploadCvButton.setOnClickListener(v -> {
            // TODO: Implement PDF picker
            Toast.makeText(this, "PDF picker coming soon", Toast.LENGTH_SHORT).show();
            // Simulate CV upload
            hasCv = true;
            updateCvStatus();
        });

        viewCvButton.setOnClickListener(v -> {
            // TODO: Open PDF viewer
            Toast.makeText(this, "Opening CV...", Toast.LENGTH_SHORT).show();
        });

        downloadCvButton.setOnClickListener(v -> {
            // TODO: Download CV
            Toast.makeText(this, "Downloading CV...", Toast.LENGTH_SHORT).show();
        });

        removeCvButton.setOnClickListener(v -> showRemoveDialog());
    }

    private void updateCvStatus() {
        if (hasCv) {
            cvStatusText.setText("CV Uploaded");
            cvFileName.setText("my_cv.pdf");
            cvFileName.setVisibility(View.VISIBLE);
            cvUploadDate.setText("Uploaded on Oct 24, 2025");
            cvUploadDate.setVisibility(View.VISIBLE);

            uploadCvButton.setText("Replace CV");
            viewCvButton.setVisibility(View.VISIBLE);
            downloadCvButton.setVisibility(View.VISIBLE);
            removeCvButton.setVisibility(View.VISIBLE);
        } else {
            cvStatusText.setText("No CV uploaded");
            cvFileName.setVisibility(View.GONE);
            cvUploadDate.setVisibility(View.GONE);

            uploadCvButton.setText("Upload CV (PDF)");
            viewCvButton.setVisibility(View.GONE);
            downloadCvButton.setVisibility(View.GONE);
            removeCvButton.setVisibility(View.GONE);
        }
    }

    private void showRemoveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove CV")
                .setMessage("Are you sure you want to remove your CV?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    hasCv = false;
                    updateCvStatus();
                    Toast.makeText(this, "CV removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}