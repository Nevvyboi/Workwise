package com.workwise.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private TextInputEditText bioInput;
    private TextInputEditText nameInput;
    private TextInputEditText sideProjectsInput;
    private TextInputEditText emailInput;
    private MaterialButton changePhotoButton;
    private MaterialButton removePhotoButton;
    private MaterialButton saveButton;
    private ImageButton backButton;

    private apiService api;
    private int userId = -1;
    private String currentProfileImagePath = null;
    private File selectedImageFile = null;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Please enable storage access in settings.",
                            Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleSelectedImage(imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsprofile);

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
            profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            Toast.makeText(this, "Photo will be removed when you save", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void checkPermissionAndOpenPicker() {
        // For Android 13+ (API 33), we don't need READ_EXTERNAL_STORAGE for image picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openImagePicker();
        } else {
            // For older versions, check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImage.launch(intent);
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            // Create temp file from URI
            selectedImageFile = createTempFileFromUri(imageUri);
            if (selectedImageFile != null) {
                // Display the image
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.outlineaccountscircle24)
                        .circleCrop()
                        .into(profileImagePreview);
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            File tempFile = new File(getCacheDir(), "temp_profile_" + System.currentTimeMillis() + ".jpg");
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

    private void loadUserProfile() {
        // First, load cached data from SharedPreferences immediately
        loadFromSharedPreferences();

        // Then fetch fresh data from API
        // Disable save button while loading
        if (saveButton != null) {
            saveButton.setEnabled(false);
            saveButton.setText("Loading...");
        }

        Call<userProfile> call = api.getProfile(userId, apiConfig.tokenProfileGet);

        call.enqueue(new Callback<userProfile>() {
            @Override
            public void onResponse(Call<userProfile> call, Response<userProfile> response) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                }

                if (response.isSuccessful() && response.body() != null) {
                    userProfile profile = response.body();
                    updateUI(profile);
                    saveToSharedPreferences(profile);
                    android.util.Log.d("PROFILE_LOAD", "Profile loaded from API successfully");
                } else {
                    android.util.Log.e("PROFILE_LOAD", "Failed to load from API: " + response.message());
                    Toast.makeText(settingsprofile.this,
                            "Using cached profile data",
                            Toast.LENGTH_SHORT).show();
                    // Keep the cached data that was loaded earlier
                }
            }

            @Override
            public void onFailure(Call<userProfile> call, Throwable t) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                }
                android.util.Log.e("PROFILE_LOAD", "Network error: " + t.getMessage());
                Toast.makeText(settingsprofile.this,
                        "Using cached profile data - Check network connection",
                        Toast.LENGTH_SHORT).show();
                // Keep the cached data that was loaded earlier
            }
        });
    }

    private void updateUI(userProfile profile) {
        runOnUiThread(() -> {
            // Set name - handle null
            if (nameInput != null) {
                if (profile.profileName != null && !profile.profileName.isEmpty()) {
                    nameInput.setText(profile.profileName);
                } else {
                    nameInput.setText("");
                    nameInput.setHint("Enter your name");
                }
            }

            // Set bio - handle null
            if (bioInput != null) {
                if (profile.profileBio != null && !profile.profileBio.isEmpty()) {
                    bioInput.setText(profile.profileBio);
                } else {
                    bioInput.setText("");
                    bioInput.setHint("Tell us about yourself");
                }
            }

            // Set email - handle null
            if (emailInput != null) {
                if (profile.email != null && !profile.email.isEmpty()) {
                    emailInput.setText(profile.email);
                } else {
                    emailInput.setText("");
                    emailInput.setHint("Enter your email");
                }
                // Usually email should not be editable after registration
                emailInput.setEnabled(false);
            }

            // Set side projects - handle null (if this field exists in your userProfile model)
            if (sideProjectsInput != null) {
                sideProjectsInput.setText("");
                sideProjectsInput.setHint("Enter your side projects (optional)");
            }

            // Load profile image - handle null
            if (profile.profileImage != null && !profile.profileImage.isEmpty()) {
                currentProfileImagePath = profile.profileImage;

                // Construct full image URL
                String imageUrl = profile.profileImage;
                if (!imageUrl.startsWith("http")) {
                    // Replace backslashes with forward slashes
                    imageUrl = imageUrl.replace("\\", "/");
                    // Remove leading slash if present
                    if (imageUrl.startsWith("/")) {
                        imageUrl = imageUrl.substring(1);
                    }
                    // Remove trailing slash from base URL if present
                    String baseUrl = apiConfig.baseUrl;
                    if (baseUrl.endsWith("/")) {
                        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    }
                    imageUrl = baseUrl + "/" + imageUrl;
                }

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.outlineaccountscircle24)
                        .error(R.drawable.outlineaccountscircle24)
                        .circleCrop()
                        .into(profileImagePreview);
            } else {
                // No profile image, show default
                profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            }
        });
    }

    private void saveToSharedPreferences(userProfile profile) {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_name", profile.profileName != null ? profile.profileName : "");
        editor.putString("user_email", profile.email != null ? profile.email : "");
        editor.putString("user_bio", profile.profileBio != null ? profile.profileBio : "");

        editor.apply();
    }

    private void loadFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);

        String cachedName = prefs.getString("user_name", "");
        String cachedEmail = prefs.getString("user_email", "");
        String cachedBio = prefs.getString("user_bio", "");

        boolean hasCache = false;

        if (nameInput != null && !TextUtils.isEmpty(cachedName)) {
            nameInput.setText(cachedName);
            hasCache = true;
        }
        if (emailInput != null && !TextUtils.isEmpty(cachedEmail)) {
            emailInput.setText(cachedEmail);
            emailInput.setEnabled(false);
            hasCache = true;
        }
        if (bioInput != null && !TextUtils.isEmpty(cachedBio)) {
            bioInput.setText(cachedBio);
            hasCache = true;
        }

        if (hasCache) {
            android.util.Log.d("PROFILE_LOAD", "Loaded cached profile data");
        } else {
            android.util.Log.d("PROFILE_LOAD", "No cached profile data found");
        }
    }

    private void saveProfile() {
        // Validate inputs
        String name = nameInput != null && nameInput.getText() != null ?
                nameInput.getText().toString().trim() : null;
        String bio = bioInput != null && bioInput.getText() != null ?
                bioInput.getText().toString().trim() : null;

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable save button during save
        if (saveButton != null) {
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
        }

        // First, upload image if selected
        if (selectedImageFile != null) {
            uploadProfileImage();
        } else {
            // Just update profile info
            updateProfileInfo();
        }
    }

    private void uploadProfileImage() {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), selectedImageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), requestFile);

        Call<profileImageUpload> call = api.uploadProfileImage(userId, body, apiConfig.tokenProfileImage);

        call.enqueue(new Callback<profileImageUpload>() {
            @Override
            public void onResponse(Call<profileImageUpload> call, Response<profileImageUpload> response) {
                if (response.isSuccessful()) {
                    // After image upload, update profile info
                    updateProfileInfo();
                } else {
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                        saveButton.setText("Save Changes");
                    }
                    Toast.makeText(settingsprofile.this,
                            "Failed to upload image: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<profileImageUpload> call, Throwable t) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                }
                Toast.makeText(settingsprofile.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileInfo() {
        userProfileUpdate update = new userProfileUpdate();

        // Get text from inputs, use null if empty
        String name = nameInput != null && nameInput.getText() != null ?
                nameInput.getText().toString().trim() : null;
        String bio = bioInput != null && bioInput.getText() != null ?
                bioInput.getText().toString().trim() : null;

        update.profileName = !TextUtils.isEmpty(name) ? name : null;
        update.profileBio = !TextUtils.isEmpty(bio) ? bio : null;

        Call<userProfile> call = api.updateProfile(userId, update, apiConfig.tokenProfileUpdate);

        call.enqueue(new Callback<userProfile>() {
            @Override
            public void onResponse(Call<userProfile> call, Response<userProfile> response) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                }

                if (response.isSuccessful() && response.body() != null) {
                    // Save to SharedPreferences for the settings page
                    saveToSharedPreferences(response.body());

                    Toast.makeText(settingsprofile.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(settingsprofile.this,
                            "Failed to update profile: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<userProfile> call, Throwable t) {
                if (saveButton != null) {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Changes");
                }
                Toast.makeText(settingsprofile.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up temp file if exists
        if (selectedImageFile != null && selectedImageFile.exists()) {
            selectedImageFile.delete();
        }
    }
}