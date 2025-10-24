package com.workwise.cv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class managecv extends AppCompatActivity {

    private static final String PREFS_NAME = "CVPrefs";
    private static final String KEY_CV_URI = "cv_uri";
    private static final String KEY_CV_NAME = "cv_name";
    private static final String KEY_CV_DATE = "cv_upload_date";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private SharedPreferences preferences;
    private ImageButton backButton;
    private MaterialCardView cvStatusCard;
    private ImageView cvIcon;
    private TextView cvStatusText;
    private TextView cvFileName;
    private TextView cvUploadDate;
    private MaterialButton uploadCvButton;
    private MaterialButton viewCvButton;
    private MaterialButton downloadCvButton;
    private MaterialButton removeCvButton;

    private Uri cvUri;
    private String cvName;

    private final ActivityResultLauncher<Intent> pickPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        handlePdfSelection(selectedUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsmanagecv);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initializeViews();
        loadCvData();
        setupListeners();
        updateUI();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        cvStatusCard = findViewById(R.id.cvStatusCard);
        cvIcon = findViewById(R.id.cvIcon);
        cvStatusText = findViewById(R.id.cvStatusText);
        cvFileName = findViewById(R.id.cvFileName);
        cvUploadDate = findViewById(R.id.cvUploadDate);
        uploadCvButton = findViewById(R.id.uploadCvButton);
        viewCvButton = findViewById(R.id.viewCvButton);
        downloadCvButton = findViewById(R.id.downloadCvButton);
        removeCvButton = findViewById(R.id.removeCvButton);
    }

    private void loadCvData() {
        String savedUri = preferences.getString(KEY_CV_URI, null);
        cvName = preferences.getString(KEY_CV_NAME, null);

        if (savedUri != null) {
            cvUri = Uri.parse(savedUri);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        uploadCvButton.setOnClickListener(v -> openFilePicker());

        viewCvButton.setOnClickListener(v -> viewCV());

        downloadCvButton.setOnClickListener(v -> downloadCV());

        removeCvButton.setOnClickListener(v -> showRemoveConfirmation());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            pickPdfLauncher.launch(Intent.createChooser(intent, "Select CV (PDF)"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open file picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePdfSelection(Uri uri) {
        try {
            // Check file size
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                long fileSize = inputStream.available();
                inputStream.close();

                if (fileSize > MAX_FILE_SIZE) {
                    Toast.makeText(this, "File too large. Maximum size is 5MB", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Get file name
            String fileName = getFileName(uri);

            // Save CV to internal storage
            File savedFile = saveCvToInternalStorage(uri, fileName);

            if (savedFile != null) {
                // Save metadata
                cvUri = Uri.fromFile(savedFile);
                cvName = fileName;
                String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

                preferences.edit()
                        .putString(KEY_CV_URI, cvUri.toString())
                        .putString(KEY_CV_NAME, fileName)
                        .putString(KEY_CV_DATE, currentDate)
                        .apply();

                Toast.makeText(this, "CV uploaded successfully", Toast.LENGTH_SHORT).show();
                updateUI();
            } else {
                Toast.makeText(this, "Failed to save CV", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error uploading CV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File saveCvToInternalStorage(Uri uri, String fileName) {
        try {
            File cvDir = new File(getFilesDir(), "cvs");
            if (!cvDir.exists()) {
                cvDir.mkdirs();
            }

            File cvFile = new File(cvDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(cvFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return cvFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "CV.pdf";

        try {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    private void viewCV() {
        if (cvUri == null) {
            Toast.makeText(this, "No CV available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File cvFile = new File(cvUri.getPath());

            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    cvFile
            );

            intent.setDataAndType(contentUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(Intent.createChooser(intent, "Open CV with"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadCV() {
        // In this implementation, the CV is already stored locally
        // This button could be used to share or export the CV
        Toast.makeText(this, "CV is stored locally", Toast.LENGTH_SHORT).show();

        // Optional: Share CV
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            File cvFile = new File(cvUri.getPath());

            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    cvFile
            );

            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share CV"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share CV", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRemoveConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Remove CV")
                .setMessage("Are you sure you want to remove your CV? This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> removeCV())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeCV() {
        try {
            // Delete file
            if (cvUri != null) {
                File cvFile = new File(cvUri.getPath());
                if (cvFile.exists()) {
                    cvFile.delete();
                }
            }

            // Clear preferences
            preferences.edit()
                    .remove(KEY_CV_URI)
                    .remove(KEY_CV_NAME)
                    .remove(KEY_CV_DATE)
                    .apply();

            cvUri = null;
            cvName = null;

            Toast.makeText(this, "CV removed successfully", Toast.LENGTH_SHORT).show();
            updateUI();

        } catch (Exception e) {
            Toast.makeText(this, "Error removing CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (cvUri != null && cvName != null) {
            // CV exists
            cvStatusText.setText("CV Uploaded");
            cvStatusText.setTextColor(getColor(R.color.colorPrimary));

            cvFileName.setText(cvName);
            cvFileName.setVisibility(View.VISIBLE);

            String uploadDate = preferences.getString(KEY_CV_DATE, "");
            if (!uploadDate.isEmpty()) {
                cvUploadDate.setText("Uploaded: " + uploadDate);
                cvUploadDate.setVisibility(View.VISIBLE);
            }

            uploadCvButton.setText("Replace CV");
            viewCvButton.setVisibility(View.VISIBLE);
            downloadCvButton.setVisibility(View.VISIBLE);
            removeCvButton.setVisibility(View.VISIBLE);

        } else {
            // No CV
            cvStatusText.setText("No CV uploaded");
            cvStatusText.setTextColor(getColor(android.R.color.darker_gray));

            cvFileName.setVisibility(View.GONE);
            cvUploadDate.setVisibility(View.GONE);

            uploadCvButton.setText("Upload CV (PDF)");
            viewCvButton.setVisibility(View.GONE);
            downloadCvButton.setVisibility(View.GONE);
            removeCvButton.setVisibility(View.GONE);
        }
    }
}