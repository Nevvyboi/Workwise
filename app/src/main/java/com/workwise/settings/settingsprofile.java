package com.workwise.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.workwise.R;

public class settingsprofile extends AppCompatActivity {

    private ImageButton backButton;
    private ImageView profileImagePreview;
    private MaterialButton changePhotoButton;
    private MaterialButton removePhotoButton;
    private TextInputEditText bioInput;
    private TextInputEditText nameInput;
    private TextInputEditText sideProjectsInput;
    private TextInputEditText emailInput;
    private MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsprofile);

        initializeViews();
        setupClickListeners();
        loadUserData();
        System.out.println(123);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        profileImagePreview = findViewById(R.id.profileImagePreview);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        bioInput = findViewById(R.id.bioInput);
        nameInput = findViewById(R.id.nameInput);
        sideProjectsInput = findViewById(R.id.sideProjectsInput);
        emailInput = findViewById(R.id.emailInput);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        changePhotoButton.setOnClickListener(v -> {
            // TODO: Implement photo picker
            Toast.makeText(this, "Photo picker coming soon", Toast.LENGTH_SHORT).show();
        });

        removePhotoButton.setOnClickListener(v -> {
            // TODO: Remove photo
            profileImagePreview.setImageResource(R.drawable.outlineaccountscircle24);
            Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        // TODO: Load from SharedPreferences or database
        if (nameInput != null) nameInput.setText("panayioti economou");
        if (emailInput != null) emailInput.setText("pano@workwise.za");
        if (bioInput != null) bioInput.setText("hi I'm pano a software engineer");
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();
        String sideProjects = sideProjectsInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }

        // TODO: Save to SharedPreferences or database
        // getSharedPreferences("UserPrefs", MODE_PRIVATE)
        //     .edit()
        //     .putString("name", name)
        //     .putString("bio", bio)
        //     .putString("sideProjects", sideProjects)
        //     .apply();

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}