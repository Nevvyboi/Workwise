package com.workwise.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.workwise.models.*;
import com.workwise.network.*;

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
    private String currentProfileImagePath = null;   // Server path (e.g. "uploads/profiles/123.jpg")
    private File selectedImageFile = null;           // Local file for upload
    private boolean shouldRemoveImage = false;       // Flag to remove image on save

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openImagePicker();
                else Toast.makeText(this, "Permission denied. Enable storage access.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Photo will be removed when you save", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    /* --------------------------------------------------------------------- */
    /*  PERMISSIONS & IMAGE PICKER                                           */
    /* --------------------------------------------------------------------- */
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
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImage.launch(intent);
    }

    private void handleSelectedImage(Uri uri) {
        selectedImageFile = createTempFileFromUri(uri);
        if (selectedImageFile != null) {
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.outlineaccountscircle24)
                    .circleCrop()
                    .into(profileImagePreview);
            shouldRemoveImage = false; // New image selected → don't remove
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            File temp = new File(getCacheDir(), "temp_profile_" + System.currentTimeMillis() + ".jpg");
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            out = new FileOutputStream(temp);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.flush();
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }

    /* --------------------------------------------------------------------- */
    /*  LOAD PROFILE FROM SERVER                                             */
    /* --------------------------------------------------------------------- */
    private void loadUserProfile() {
        saveButton.setEnabled(false);
        saveButton.setText("Loading...");

        Call<UserProfileOut> call = api.getProfile(userId, apiConfig.tokenProfileGet);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override
            public void onResponse(Call<UserProfileOut> call, Response<UserProfileOut> response) {
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileOut profile = response.body();
                    updateUI(profile);
                    saveToSharedPreferences(profile);
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

    private void updateUI(UserProfileOut profile) {
        runOnUiThread(() -> {
            nameInput.setText(profile.profileName != null ? profile.profileName : "");
            bioInput.setText(profile.profileBio != null ? profile.profileBio : "");
            emailInput.setText(profile.email != null ? profile.email : "");
            emailInput.setEnabled(false);
            sideProjectsInput.setText(profile.sideProjects != null ? profile.sideProjects : "");

            currentProfileImagePath = profile.profileImage;
            if (!TextUtils.isEmpty(currentProfileImagePath)) {
                String url = buildFullImageUrl(currentProfileImagePath);
                Glide.with(settingsprofile.this)
                        .load(url)
                        .placeholder(R.drawable.outlineaccountscircle24)
                        .error(R.drawable.outlineaccountscircle24)
                        .circleCrop()
                        .into(profileImagePreview);
            } else {
                profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            }
        });
    }

    private String buildFullImageUrl(String relativePath) {
        String path = relativePath.replace("\\", "/");
        if (path.startsWith("/")) path = path.substring(1);
        String base = apiConfig.baseUrl.endsWith("/") ? apiConfig.baseUrl.substring(0, apiConfig.baseUrl.length() - 1) : apiConfig.baseUrl;
        return base + "/" + path;
    }

    private void saveToSharedPreferences(UserProfileOut profile) {
        SharedPreferences.Editor editor = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE).edit();
        editor.putString("user_name", profile.profileName != null ? profile.profileName : "");
        editor.putString("user_email", profile.email != null ? profile.email : "");
        editor.putString("user_bio", profile.profileBio != null ? profile.profileBio : "");
        editor.putString("user_profile_image", profile.profileImage != null ? profile.profileImage : "");
        editor.putString("user_side_projects", profile.sideProjects != null ? profile.sideProjects : "");
        editor.apply();
    }

    private void loadFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        nameInput.setText(prefs.getString("user_name", ""));
        bioInput.setText(prefs.getString("user_bio", ""));
        emailInput.setText(prefs.getString("user_email", ""));
        sideProjectsInput.setText(prefs.getString("user_side_projects", ""));
        String img = prefs.getString("user_profile_image", null);
        if (!TextUtils.isEmpty(img)) {
            currentProfileImagePath = img;
            Glide.with(this).load(buildFullImageUrl(img)).into(profileImagePreview);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  SAVE PROFILE: Image → Text                                           */
    /* --------------------------------------------------------------------- */
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
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), selectedImageFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), reqFile);

        Call<ProfileImageUploadResponse> call = api.uploadProfileImage(userId, part, apiConfig.tokenProfileImage);
        call.enqueue(new Callback<ProfileImageUploadResponse>() {
            @Override
            public void onResponse(Call<ProfileImageUploadResponse> call, Response<ProfileImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfileImagePath = response.body().filePath;
                    shouldRemoveImage = false;
                    updateProfileInfo();  // Now update text + new image path
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
        upd.profileImage = "";  // Tell backend to remove

        Call<UserProfileOut> call = api.updateProfile(userId, upd, apiConfig.tokenProfileUpdate);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override
            public void onResponse(Call<UserProfileOut> call, Response<UserProfileOut> resp) {
                handleUpdateResponse(resp);
            }

            @Override
            public void onFailure(Call<UserProfileOut> call, Throwable t) {
                enableSaveButton();
                Toast.makeText(settingsprofile.this, "Failed to remove image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileInfo() {
        UpdateProfileIn upd = new UpdateProfileIn();
        upd.profileName = nameInput.getText().toString().trim();
        upd.profileBio = bioInput.getText().toString().trim();
        upd.sideProjects = sideProjectsInput.getText().toString().trim();

        Call<UserProfileOut> call = api.updateProfile(userId, upd, apiConfig.tokenProfileUpdate);
        call.enqueue(new Callback<UserProfileOut>() {
            @Override
            public void onResponse(Call<UserProfileOut> call, Response<UserProfileOut> resp) {
                handleUpdateResponse(resp);
            }

            @Override
            public void onFailure(Call<UserProfileOut> call, Throwable t) {
                enableSaveButton();
                Toast.makeText(settingsprofile.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateResponse(Response<UserProfileOut> resp) {
        enableSaveButton();
        shouldRemoveImage = false;
        if (resp.isSuccessful() && resp.body() != null) {
            saveToSharedPreferences(resp.body());
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed: " + resp.message(), Toast.LENGTH_SHORT).show();
        }
    }

    private void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setText("Save Changes");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selectedImageFile != null && selectedImageFile.exists()) {
            selectedImageFile.delete();
        }
    }
}