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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.R;
import com.workwise.models.apiResponse;
import com.workwise.models.cvItem;
import com.workwise.models.cvUpload;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
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
    private cvItem currentCv;
    private Call<List<cvItem>> activeListCall;
    private Call<cvUpload> activeUploadCall;
    private Call<apiResponse> activeDeleteCall;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openFilePicker();
                else Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<Intent> pickFile =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) uploadCV(fileUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.settingsmanagecv);
        initializeViews();

        try {
            api = apiClient.get().create(apiService.class);
        } catch (Exception e) {
            Log.e("CV_API", "Retrofit init failed", e);
            Toast.makeText(this, "API init error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserCVs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelActiveCalls();
    }

    private void cancelActiveCalls() {
        if (activeListCall != null) activeListCall.cancel();
        if (activeUploadCall != null) activeUploadCall.cancel();
        if (activeDeleteCall != null) activeDeleteCall.cancel();
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
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (uploadCvButton != null) uploadCvButton.setOnClickListener(v -> checkPermissionAndOpenPicker());
        if (viewCvButton != null) viewCvButton.setOnClickListener(v -> {
            if (currentCv != null) viewCV();
        });
        if (downloadCvButton != null) downloadCvButton.setOnClickListener(v -> {
            if (currentCv != null) downloadCV();
        });
        if (removeCvButton != null) removeCvButton.setOnClickListener(v -> {
            if (currentCv != null) confirmDelete();
        });
    }

    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickFile.launch(intent);
    }

    private void loadUserCVs() {
        if (api == null || userId == -1) {
            updateUINoCV();
            return;
        }
        if (uploadCvButton != null) uploadCvButton.setEnabled(false);

        activeListCall = api.getCVs(userId, apiConfig.tokenCvList);
        activeListCall.enqueue(new Callback<List<cvItem>>() {
            @Override
            public void onResponse(Call<List<cvItem>> call, Response<List<cvItem>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (uploadCvButton != null) uploadCvButton.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    updateUINoCV();
                    return;
                }
                List<cvItem> cvs = response.body();
                if (cvs.isEmpty()) {
                    updateUINoCV();
                    return;
                }
                currentCv = cvs.get(0);
                for (cvItem c : cvs) {
                    if (c.isPrimary) {
                        currentCv = c;
                        break;
                    }
                }
                updateUIWithCV();
            }
            @Override
            public void onFailure(Call<List<cvItem>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (uploadCvButton != null) uploadCvButton.setEnabled(true);
                updateUINoCV();
            }
        });
    }

    private void uploadCV(Uri fileUri) {
        if (api == null) {
            Toast.makeText(this, "API unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = getFileName(fileUri);
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            Toast.makeText(this, "Select a PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uploadCvButton != null) {
            uploadCvButton.setEnabled(false);
            uploadCvButton.setText("Uploading...");
        }
        File tempFile = createTempFileFromUri(fileUri, fileName);
        if (tempFile == null) {
            if (uploadCvButton != null) {
                uploadCvButton.setEnabled(true);
                uploadCvButton.setText("Upload CV");
            }
            Toast.makeText(this, "File read error", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), tempFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);
        RequestBody isPrimary = RequestBody.create(MediaType.parse("text/plain"), "true");

        activeUploadCall = api.uploadCV(userId, body, isPrimary, apiConfig.tokenCvUpload);
        activeUploadCall.enqueue(new Callback<cvUpload>() {
            @Override
            public void onResponse(Call<cvUpload> call, Response<cvUpload> response) {
                if (tempFile.exists()) tempFile.delete();
                if (isFinishing() || isDestroyed()) return;
                if (uploadCvButton != null) {
                    uploadCvButton.setEnabled(true);
                    uploadCvButton.setText("Upload CV");
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(settingsmanagecv.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadUserCVs();
            }
            @Override
            public void onFailure(Call<cvUpload> call, Throwable t) {
                if (tempFile.exists()) tempFile.delete();
                if (isFinishing() || isDestroyed()) return;
                if (uploadCvButton != null) {
                    uploadCvButton.setEnabled(true);
                    uploadCvButton.setText("Upload CV");
                }
                Toast.makeText(settingsmanagecv.this, "Upload error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewCV() {
        if (currentCv == null || currentCv.filePath == null) {
            Toast.makeText(this, "CV unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        String pdfUrl = buildFullUrl(currentCv.filePath);
        Intent intent = new Intent(this, pdfviewer.class);
        intent.putExtra(pdfviewer.EXTRA_PDF_URL, pdfUrl);
        intent.putExtra(pdfviewer.EXTRA_FILE_NAME, currentCv.cvName);
        startActivity(intent);
    }

    private String buildFullUrl(String relativePath) {
        if (relativePath == null) return "";
        String url = relativePath.replace("\\", "/");
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) url = url.substring(1);
            String base = apiConfig.baseUrl.endsWith("/") ?
                    apiConfig.baseUrl.substring(0, apiConfig.baseUrl.length() - 1) :
                    apiConfig.baseUrl;
            url = base + "/" + url;
        }
        return url;
    }

    private void downloadCV() {
        viewCV();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Remove CV")
                .setMessage("Remove current CV?")
                .setPositiveButton("Remove", (d, w) -> deleteCV())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCV() {
        if (api == null || currentCv == null) return;
        if (removeCvButton != null) removeCvButton.setEnabled(false);
        activeDeleteCall = api.deleteCV(userId, currentCv.cvId, apiConfig.tokenCvDelete);
        activeDeleteCall.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(Call<apiResponse> call, Response<apiResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (removeCvButton != null) removeCvButton.setEnabled(true);
                if (!response.isSuccessful()) {
                    Toast.makeText(settingsmanagecv.this, "Delete failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentCv = null;
                updateUINoCV();
            }
            @Override
            public void onFailure(Call<apiResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (removeCvButton != null) removeCvButton.setEnabled(true);
                Toast.makeText(settingsmanagecv.this, "Delete error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithCV() {
        if (cvStatusText == null) return;
        cvStatusText.setText("CV Uploaded");
        if (cvFileName != null) {
            cvFileName.setText(currentCv.cvName);
            cvFileName.setVisibility(View.VISIBLE);
        }
        if (cvUploadDate != null) {
            String uploadDate = currentCv.uploadedAt;
            if (uploadDate != null && uploadDate.length() >= 10) uploadDate = uploadDate.substring(0, 10);
            cvUploadDate.setText("Uploaded: " + uploadDate);
            cvUploadDate.setVisibility(View.VISIBLE);
        }
        if (viewCvButton != null) viewCvButton.setVisibility(View.VISIBLE);
        if (downloadCvButton != null) downloadCvButton.setVisibility(View.VISIBLE);
        if (removeCvButton != null) removeCvButton.setVisibility(View.VISIBLE);
    }

    private void updateUINoCV() {
        if (cvStatusText != null) cvStatusText.setText("No CV uploaded");
        if (cvFileName != null) cvFileName.setVisibility(View.GONE);
        if (cvUploadDate != null) cvUploadDate.setVisibility(View.GONE);
        if (viewCvButton != null) viewCvButton.setVisibility(View.GONE);
        if (downloadCvButton != null) downloadCvButton.setVisibility(View.GONE);
        if (removeCvButton != null) removeCvButton.setVisibility(View.GONE);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && "content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private File createTempFileFromUri(Uri uri, String fileName) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            File temp = new File(getCacheDir(), "temp_" + System.currentTimeMillis() + "_" + fileName);
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            out = new FileOutputStream(temp);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.flush();
            return temp;
        } catch (Exception e) {
            return null;
        } finally {
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }
}
