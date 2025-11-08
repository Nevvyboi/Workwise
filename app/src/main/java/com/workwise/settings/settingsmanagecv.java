package com.workwise.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;
import com.workwise.models.*;
import com.workwise.network.*;
import com.workwise.pdfviewer.pdfviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class settingsmanagecv extends AppCompatActivity {

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

    private apiService api;
    private int userId = -1;
    private cvItem currentCv = null;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openFilePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Please enable storage access in settings.",
                            Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickFile =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadCV(fileUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsmanagecv);

        // Initialize API
        api = apiClient.get().create(apiService.class);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadUserCVs();
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

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        uploadCvButton.setOnClickListener(v -> checkPermissionAndOpenPicker());

        viewCvButton.setOnClickListener(v -> {
            if (currentCv != null) {
                viewCV();
            }
        });

        downloadCvButton.setOnClickListener(v -> {
            if (currentCv != null) {
                downloadCV();
            }
        });

        removeCvButton.setOnClickListener(v -> {
            if (currentCv != null) {
                deleteCV();
            }
        });
    }

    private void checkPermissionAndOpenPicker() {
        // For Android 13+ (API 33), we don't need READ_EXTERNAL_STORAGE for document picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else {
            // For older versions, check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openFilePicker();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        // Allow access to content from external sources
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickFile.launch(intent);
    }

    private void loadUserCVs() {
        // Show loading state
        uploadCvButton.setEnabled(false);

        Call<List<cvItem>> call = api.getCVs(userId, apiConfig.tokenCvList);

        call.enqueue(new Callback<List<cvItem>>() {
            @Override
            public void onResponse(Call<List<cvItem>> call, Response<List<cvItem>> response) {
                uploadCvButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    List<cvItem> cvs = response.body();
                    if (!cvs.isEmpty()) {
                        // Get the primary CV or the first one
                        currentCv = cvs.get(0);
                        for (cvItem cv : cvs) {
                            if (cv.isPrimary) {
                                currentCv = cv;
                                break;
                            }
                        }
                        updateUIWithCV();
                    } else {
                        updateUINoCV();
                    }
                } else {
                    Toast.makeText(settingsmanagecv.this,
                            "Failed to load CVs: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                    updateUINoCV();
                }
            }

            @Override
            public void onFailure(Call<List<cvItem>> call, Throwable t) {
                uploadCvButton.setEnabled(true);
                Toast.makeText(settingsmanagecv.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                updateUINoCV();
            }
        });
    }

    private void uploadCV(Uri fileUri) {
        try {
            // Get file name
            String fileName = getFileName(fileUri);

            if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                Toast.makeText(this, "Please select a valid PDF file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show uploading state
            uploadCvButton.setEnabled(false);
            uploadCvButton.setText("Uploading...");

            // Create temp file
            File tempFile = createTempFileFromUri(fileUri, fileName);
            if (tempFile == null) {
                Toast.makeText(this, "Failed to prepare file", Toast.LENGTH_SHORT).show();
                uploadCvButton.setEnabled(true);
                uploadCvButton.setText("Upload CV");
                return;
            }

            // Create request body
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);
            RequestBody isPrimary = RequestBody.create(MediaType.parse("text/plain"), "true");

            // Upload
            Call<cvUpload> call = api.uploadCV(userId, body, isPrimary, apiConfig.tokenCvUpload);

            call.enqueue(new Callback<cvUpload>() {
                @Override
                public void onResponse(Call<cvUpload> call, Response<cvUpload> response) {
                    // Clean up temp file
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }

                    uploadCvButton.setEnabled(true);
                    uploadCvButton.setText("Upload CV");

                    if (response.isSuccessful()) {
                        cvUpload uploadResponse = response.body();
                        if (uploadResponse != null) {
                            android.util.Log.d("CV_UPLOAD", "File uploaded to: " + uploadResponse.filePath);
                            android.util.Log.d("CV_UPLOAD", "Full URL would be: " + apiConfig.baseUrl + "/" + uploadResponse.filePath);
                        }
                        Toast.makeText(settingsmanagecv.this,
                                "CV uploaded successfully!",
                                Toast.LENGTH_SHORT).show();
                        loadUserCVs(); // Reload CVs
                    } else {
                        Toast.makeText(settingsmanagecv.this,
                                "Failed to upload CV: " + response.message(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<cvUpload> call, Throwable t) {
                    // Clean up temp file
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }

                    uploadCvButton.setEnabled(true);
                    uploadCvButton.setText("Upload CV");

                    Toast.makeText(settingsmanagecv.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            uploadCvButton.setEnabled(true);
            uploadCvButton.setText("Upload CV");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void viewCV() {
        if (currentCv == null) return;

        // Construct the full PDF URL
        String pdfUrl = currentCv.filePath;
        android.util.Log.d("CV_VIEW", "Original filePath: " + pdfUrl);

        // Normalize path separators (convert backslashes to forward slashes)
        pdfUrl = pdfUrl.replace("\\", "/");

        if (!pdfUrl.startsWith("http")) {
            // Remove leading slash if present to avoid double slashes
            if (pdfUrl.startsWith("/")) {
                pdfUrl = pdfUrl.substring(1);
            }

            // Remove trailing slash from base URL if present
            String baseUrl = apiConfig.baseUrl;
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            pdfUrl = baseUrl + "/" + pdfUrl;
        }

        android.util.Log.d("CV_VIEW", "Final PDF URL: " + pdfUrl);
        android.util.Log.d("CV_VIEW", "Base URL: " + apiConfig.baseUrl);

        // Launch PDF viewer
        Intent intent = new Intent(this, pdfviewer.class);
        intent.putExtra(pdfviewer.EXTRA_PDF_URL, pdfUrl);
        intent.putExtra(pdfviewer.EXTRA_FILE_NAME, currentCv.cvName);
        startActivity(intent);
    }

    private void downloadCV() {
        if (currentCv == null) return;

        // For now, just open the viewer
        // In a full implementation, you would download to external storage
        Toast.makeText(this, "Opening CV viewer...", Toast.LENGTH_SHORT).show();
        viewCV();
    }

    private void deleteCV() {
        if (currentCv == null) return;

        // Disable button during deletion
        removeCvButton.setEnabled(false);

        Call<apiResponse> call = api.deleteCV(userId, currentCv.cvId, apiConfig.tokenCvDelete);

        call.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(Call<apiResponse> call, Response<apiResponse> response) {
                removeCvButton.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(settingsmanagecv.this,
                            "CV removed successfully!",
                            Toast.LENGTH_SHORT).show();
                    currentCv = null;
                    updateUINoCV();
                } else {
                    Toast.makeText(settingsmanagecv.this,
                            "Failed to remove CV: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<apiResponse> call, Throwable t) {
                removeCvButton.setEnabled(true);
                Toast.makeText(settingsmanagecv.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithCV() {
        runOnUiThread(() -> {
            cvStatusText.setText("CV Uploaded");
            cvFileName.setText(currentCv.cvName);
            cvFileName.setVisibility(View.VISIBLE);

            // Format date nicely
            String uploadDate = currentCv.uploadedAt;
            if (uploadDate != null && uploadDate.length() >= 10) {
                uploadDate = uploadDate.substring(0, 10);
            }
            cvUploadDate.setText("Uploaded: " + uploadDate);
            cvUploadDate.setVisibility(View.VISIBLE);

            viewCvButton.setVisibility(View.VISIBLE);
            downloadCvButton.setVisibility(View.VISIBLE);
            removeCvButton.setVisibility(View.VISIBLE);
        });
    }

    private void updateUINoCV() {
        runOnUiThread(() -> {
            cvStatusText.setText("No CV uploaded");
            cvFileName.setVisibility(View.GONE);
            cvUploadDate.setVisibility(View.GONE);

            viewCvButton.setVisibility(View.GONE);
            downloadCvButton.setVisibility(View.GONE);
            removeCvButton.setVisibility(View.GONE);
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private File createTempFileFromUri(Uri uri, String fileName) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            File tempFile = new File(getCacheDir(), "temp_" + System.currentTimeMillis() + "_" + fileName);
            inputStream = getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                return null;
            }

            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            return tempFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}