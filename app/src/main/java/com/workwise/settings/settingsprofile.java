package com.workwise.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.workwise.R;
import com.workwise.models.ProfileImageUploadResponse;
import com.workwise.models.UpdateProfileIn;
import com.workwise.models.UserProfileOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class settingsprofile extends AppCompatActivity {

    private ImageView profileImagePreview;
    private TextInputEditText bioInput, nameInput, sideProjectsInput, emailInput;
    private MaterialButton changePhotoButton, removePhotoButton, saveButton;
    private ImageButton backButton;

    private apiService api;
    private int userId = -1;
    private String currentProfileImagePath = null;
    private File selectedImageFile = null;
    private boolean shouldRemoveImage = false;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openImagePicker();
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) handleSelectedImage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsprofile);

        api = apiClient.get().create(apiService.class);

        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadUserProfile();
    }

    private void initializeViews() {
        profileImagePreview = findViewById(R.id.profileImagePreview);
        bioInput = findViewById(R.id.bioInput);
        nameInput = findViewById(R.id.nameInput);
        sideProjectsInput = findViewById(R.id.sideProjectsInput);
        emailInput = findViewById(R.id.emailInput);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        changePhotoButton.setOnClickListener(v -> checkPermissionAndOpenPicker());

        removePhotoButton.setOnClickListener(v -> {
            selectedImageFile = null;
            currentProfileImagePath = null;
            shouldRemoveImage = true;
            profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            Toast.makeText(this, "Photo will be removed on save", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    /* ---------- PERMISSIONS & IMAGE PICKER ---------- */
    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openImagePicker();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImage.launch(intent);
    }

    private void handleSelectedImage(Uri uri) {
        selectedImageFile = createTempFileFromUri(uri);
        if (selectedImageFile != null) {
            Glide.with(this).load(uri)
                    .placeholder(R.drawable.outlineaccountscircle24)
                    .circleCrop()
                    .into(profileImagePreview);
            shouldRemoveImage = false;
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    // --- THIS METHOD IS NOW FIXED ---
    private File createTempFileFromUri(Uri uri) {
        // Create the destination file object FIRST
        File destinationFile = new File(getCacheDir(), "temp_profile_" + System.currentTimeMillis() + ".jpg");

        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush(); // Ensure all data is written

            // Now that the file is written, just return the file object
            return destinationFile;

        } catch (Exception e) {
            e.printStackTrace();
            // If it failed, delete the partially created file
            if (destinationFile != null && destinationFile.exists()) {
                destinationFile.delete();
            }
            return null;
        }
    }

    /* ---------- LOAD PROFILE ---------- */
    private void loadUserProfile() {
        saveButton.setEnabled(false);
        saveButton.setText("Loading...");

        Call<UserProfileOut> call = api.getProfile(userId, apiConfig.tokenProfileGet);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override
            public void onResponse(Call<UserProfileOut> call, Response<UserProfileOut> resp) {
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                if (resp.isSuccessful() && resp.body() != null) {
                    updateUI(resp.body());
                    saveToSharedPreferences(resp.body());
                } else {
                    Toast.makeText(settingsprofile.this, "Using cached data", Toast.LENGTH_SHORT).show();
                    loadFromSharedPreferences();
                }
            }

            @Override
            public void onFailure(Call<UserProfileOut> call, Throwable t) {
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");
                Toast.makeText(settingsprofile.this, "Network error – using cache", Toast.LENGTH_LONG).show();
                loadFromSharedPreferences();
            }
        });
    }

    private void updateUI(UserProfileOut p) {
        runOnUiThread(() -> {
            nameInput.setText(p.profileName != null ? p.profileName : "");
            bioInput.setText(p.profileBio != null ? p.profileBio : "");
            emailInput.setText(p.email != null ? p.email : "");
            emailInput.setEnabled(false);
            sideProjectsInput.setText(p.sideProjects != null ? p.sideProjects : "");

            currentProfileImagePath = p.profileImage;
            if (!TextUtils.isEmpty(currentProfileImagePath)) {
                Glide.with(this)
                        .load(buildFullImageUrl(currentProfileImagePath))
                        .placeholder(R.drawable.outlineaccountscircle24)
                        .circleCrop()
                        .into(profileImagePreview);
            } else {
                profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            }
        });
    }

    private String buildFullImageUrl(String rel) {
        String p = rel.replace("\\", "/");
        if (p.startsWith("/")) p = p.substring(1);
        String base = apiConfig.baseUrl.endsWith("/") ? apiConfig.baseUrl.substring(0, apiConfig.baseUrl.length() - 1) : apiConfig.baseUrl;
        return base + "/" + p;
    }

    private void saveToSharedPreferences(UserProfileOut p) {
        SharedPreferences.Editor e = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE).edit();
        e.putString("user_name", p.profileName != null ? p.profileName : "");
        e.putString("user_email", p.email != null ? p.email : "");
        e.putString("user_bio", p.profileBio != null ? p.profileBio : "");
        e.putString("user_profile_image", p.profileImage != null ? p.profileImage : "");
        e.putString("user_side_projects", p.sideProjects != null ? p.sideProjects : "");
        e.apply();
    }

    private void loadFromSharedPreferences() {
        SharedPreferences p = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        nameInput.setText(p.getString("user_name", ""));
        bioInput.setText(p.getString("user_bio", ""));
        emailInput.setText(p.getString("user_email", ""));
        sideProjectsInput.setText(p.getString("user_side_projects", ""));
        String img = p.getString("user_profile_image", null);
        if (!TextUtils.isEmpty(img)) {
            currentProfileImagePath = img;
            Glide.with(this).load(buildFullImageUrl(img)).into(profileImagePreview);
        }
    }

    /* ---------- SAVE PROFILE ---------- */
    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        if (selectedImageFile != null) {
            uploadProfileImage();
        } else if (shouldRemoveImage) {
            removeImageAndUpdate();
        } else {
            updateProfileInfo();
        }
    }

    private void uploadProfileImage() {
        RequestBody rb = RequestBody.create(MediaType.parse("image/*"), selectedImageFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), rb);

        Call<ProfileImageUploadResponse> call = api.uploadProfileImage(userId, part, apiConfig.tokenProfileImage);
        call.enqueue(new Callback<ProfileImageUploadResponse>() {
            @Override
            public void onResponse(Call<ProfileImageUploadResponse> call, Response<ProfileImageUploadResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    currentProfileImagePath = r.body().filePath;
                    shouldRemoveImage = false;
                    updateProfileInfo();
                } else {
                    enableSaveButton();
                    Toast.makeText(settingsprofile.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileImageUploadResponse> call, Throwable t) {
                enableSaveButton();
                Toast.makeText(settingsprofile.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeImageAndUpdate() {
        UpdateProfileIn upd = new UpdateProfileIn();
        upd.profileName = nameInput.getText().toString().trim();
        upd.profileBio = bioInput.getText().toString().trim();
        upd.sideProjects = sideProjectsInput.getText().toString().trim();
        upd.profileImage = "";               // tell backend to delete

        Call<UserProfileOut> call = api.updateProfile(userId, upd, apiConfig.tokenProfileUpdate);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override public void onResponse(Call<UserProfileOut> c, Response<UserProfileOut> r) { handleUpdateResponse(r); }
            @Override public void onFailure(Call<UserProfileOut> c, Throwable t) {
                enableSaveButton();
                Toast.makeText(settingsprofile.this, "Failed to remove image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileInfo() {
        UpdateProfileIn upd = new UpdateProfileIn();

        String n = nameInput.getText().toString().trim();
        String b = bioInput.getText().toString().trim();
        String s = sideProjectsInput.getText().toString().trim();

        upd.profileName   = TextUtils.isEmpty(n) ? "" : n;
        upd.profileBio    = TextUtils.isEmpty(b) ? "" : b;
        upd.sideProjects  = TextUtils.isEmpty(s) ? "" : s;

        Call<UserProfileOut> call = api.updateProfile(userId, upd, apiConfig.tokenProfileUpdate);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override public void onResponse(Call<UserProfileOut> c, Response<UserProfileOut> r) { handleUpdateResponse(r); }
            @Override public void onFailure(Call<UserProfileOut> c, Throwable t) {
                enableSaveButton();
                Toast.makeText(settingsprofile.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateResponse(Response<UserProfileOut> r) {
        enableSaveButton();
        shouldRemoveImage = false;
        if (r.isSuccessful() && r.body() != null) {
            saveToSharedPreferences(r.body());
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed: " + r.message(), Toast.LENGTH_SHORT).show();
        }
    }

    private void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setText("Save Changes");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selectedImageFile != null && selectedImageFile.exists()) selectedImageFile.delete();
    }
}