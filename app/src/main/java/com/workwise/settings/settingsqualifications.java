package com.workwise.settings;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.workwise.R;
import com.workwise.authentication;
import com.workwise.models.*;
import com.workwise.network.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class settingsqualifications extends AppCompatActivity {

    private ImageButton backButton;

    // Form inputs
    private TextInputLayout qualificationNameLayout, qualificationTypeLayout, institutionLayout;
    private TextInputLayout fieldOfStudyLayout, startDateLayout, endDateLayout;
    private TextInputLayout gradeLayout, descriptionLayout;

    private TextInputEditText qualificationNameInput, institutionInput, fieldOfStudyInput;
    private TextInputEditText startDateInput, endDateInput, gradeInput, descriptionInput;
    private AutoCompleteTextView qualificationTypeInput;
    private MaterialCheckBox currentlyStudyingCheckbox;

    private MaterialButton addButton, clearButton;
    private LinearLayout qualificationsContainer;
    private TextView qualificationsLabel;

    private apiService api;
    private int userId = -1;
    private boolean isLoading = false;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private Calendar selectedStartDate = Calendar.getInstance();
    private Calendar selectedEndDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Enable Edge-to-Edge Display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.settingsqualifications);

        // Initialize API
        api = apiClient.get().create(apiService.class);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadQualifications();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);

        // Layouts
        qualificationNameLayout = findViewById(R.id.qualificationNameLayout);
        qualificationTypeLayout = findViewById(R.id.qualificationTypeLayout);
        institutionLayout = findViewById(R.id.institutionLayout);
        fieldOfStudyLayout = findViewById(R.id.fieldOfStudyLayout);
        startDateLayout = findViewById(R.id.startDateLayout);
        endDateLayout = findViewById(R.id.endDateLayout);
        gradeLayout = findViewById(R.id.gradeLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);

        // Inputs
        qualificationNameInput = findViewById(R.id.qualificationNameInput);
        qualificationTypeInput = findViewById(R.id.qualificationTypeInput);
        institutionInput = findViewById(R.id.institutionInput);
        fieldOfStudyInput = findViewById(R.id.fieldOfStudyInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        gradeInput = findViewById(R.id.gradeInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        currentlyStudyingCheckbox = findViewById(R.id.currentlyStudyingCheckbox);

        // Buttons
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);

        // Other views
        qualificationsContainer = findViewById(R.id.qualificationsContainer);
        qualificationsLabel = findViewById(R.id.qualificationsLabel);

        // Setup qualification type dropdown
        setupQualificationTypeDropdown();
    }

    private void setupClickListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (addButton != null) {
            addButton.setOnClickListener(v -> addQualification());
        }

        if (clearButton != null) {
            clearButton.setOnClickListener(v -> clearForm());
        }

        // Date picker for start date
        if (startDateInput != null) {
            startDateInput.setOnClickListener(v -> showDatePicker(true));
        }

        // Date picker for end date
        if (endDateInput != null) {
            endDateInput.setOnClickListener(v -> showDatePicker(false));
        }

        // Handle currently studying checkbox
        if (currentlyStudyingCheckbox != null) {
            currentlyStudyingCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (endDateInput != null && endDateLayout != null) {
                    if (isChecked) {
                        endDateInput.setText("Present");
                        endDateInput.setEnabled(false);
                        endDateLayout.setEnabled(false);
                    } else {
                        if (endDateInput.getText().toString().equals("Present")) {
                            endDateInput.setText("");
                        }
                        endDateInput.setEnabled(true);
                        endDateLayout.setEnabled(true);
                    }
                }
            });
        }
    }

    private void setupQualificationTypeDropdown() {
        String[] qualificationTypes = {
                "Bachelor's Degree",
                "Master's Degree",
                "Doctorate/PhD",
                "Associate Degree",
                "Diploma",
                "Certificate",
                "High School",
                "Matriculation",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                qualificationTypes
        );

        if (qualificationTypeInput != null) {
            qualificationTypeInput.setAdapter(adapter);
            qualificationTypeInput.setText("Bachelor's Degree", false); // Set default
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? selectedStartDate : selectedEndDate;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);

                    String formattedDate = dateFormat.format(calendar.getTime());

                    if (isStartDate && startDateInput != null) {
                        startDateInput.setText(formattedDate);
                    } else if (!isStartDate && endDateInput != null) {
                        endDateInput.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                1
        );

        datePickerDialog.show();
    }

    private void clearForm() {
        if (qualificationNameInput != null) qualificationNameInput.setText("");
        if (institutionInput != null) institutionInput.setText("");
        if (fieldOfStudyInput != null) fieldOfStudyInput.setText("");
        if (startDateInput != null) startDateInput.setText("");
        if (endDateInput != null) endDateInput.setText("");
        if (gradeInput != null) gradeInput.setText("");
        if (descriptionInput != null) descriptionInput.setText("");
        if (currentlyStudyingCheckbox != null) currentlyStudyingCheckbox.setChecked(false);
        if (qualificationTypeInput != null) qualificationTypeInput.setText("Bachelor's Degree", false);

        // Clear errors
        clearErrors();
    }

    private void clearErrors() {
        if (qualificationNameLayout != null) qualificationNameLayout.setError(null);
        if (qualificationTypeLayout != null) qualificationTypeLayout.setError(null);
        if (institutionLayout != null) institutionLayout.setError(null);
        if (fieldOfStudyLayout != null) fieldOfStudyLayout.setError(null);
        if (startDateLayout != null) startDateLayout.setError(null);
        if (endDateLayout != null) endDateLayout.setError(null);
        if (gradeLayout != null) gradeLayout.setError(null);
        if (descriptionLayout != null) descriptionLayout.setError(null);
    }

    private void loadQualifications() {
        if (isLoading) return;
        isLoading = true;

        // Show loading state
        showLoadingState();

        Call<List<qualification>> call = api.getQualifications(userId, apiConfig.tokenQualList);

        call.enqueue(new Callback<List<qualification>>() {
            @Override
            public void onResponse(Call<List<qualification>> call, Response<List<qualification>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    displayQualifications(response.body());
                } else {
                    showErrorState("Failed to load qualifications");
                }
            }

            @Override
            public void onFailure(Call<List<qualification>> call, Throwable t) {
                isLoading = false;
                showErrorState("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoadingState() {
        if (qualificationsContainer == null) return;
        qualificationsContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading qualifications...");
        loadingText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loadingText.setTextColor(0xFF6D7A88);
        loadingText.setPadding(32, 48, 32, 48);
        loadingText.setTextSize(14);
        qualificationsContainer.addView(loadingText);
    }

    private void showErrorState(String message) {
        if (qualificationsContainer == null) return;
        qualificationsContainer.removeAllViews();

        TextView errorText = new TextView(this);
        errorText.setText(message);
        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorText.setTextColor(0xFFD32F2F);
        errorText.setPadding(32, 48, 32, 48);
        errorText.setTextSize(14);
        qualificationsContainer.addView(errorText);

        // Add retry button
        MaterialButton retryButton = new MaterialButton(this);
        retryButton.setText("Retry");
        retryButton.setOnClickListener(v -> loadQualifications());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        retryButton.setLayoutParams(params);
        qualificationsContainer.addView(retryButton);
    }

    private void displayQualifications(List<qualification> qualifications) {
        if (qualificationsContainer == null) return;

        // Clear existing views
        qualificationsContainer.removeAllViews();

        if (qualifications.isEmpty()) {
            showEmptyState();
            return;
        }

        // Update label with count
        if (qualificationsLabel != null) {
            qualificationsLabel.setText("Your Qualifications (" + qualifications.size() + ")");
        }

        for (qualification q : qualifications) {
            addQualificationCard(q);
        }
    }

    private void showEmptyState() {
        // Update label
        if (qualificationsLabel != null) {
            qualificationsLabel.setText("Your Qualifications");
        }

        // Create empty state card
        MaterialCardView emptyCard = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emptyCard.setLayoutParams(cardParams);
        emptyCard.setRadius(12 * getResources().getDisplayMetrics().density);
        emptyCard.setCardElevation(2 * getResources().getDisplayMetrics().density);
        emptyCard.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(48, 48, 48, 48);

        TextView emptyText = new TextView(this);
        emptyText.setText("No qualifications added yet");
        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyText.setTextColor(0xFF1A1A1A);
        emptyText.setTextSize(16);
        emptyText.setTypeface(null, android.graphics.Typeface.BOLD);
        contentLayout.addView(emptyText);

        TextView subText = new TextView(this);
        subText.setText("Add your first qualification using the form above");
        subText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        subText.setTextColor(0xFF6D7A88);
        subText.setTextSize(14);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = 8;
        subText.setLayoutParams(subParams);
        contentLayout.addView(subText);

        emptyCard.addView(contentLayout);
        qualificationsContainer.addView(emptyCard);
    }

    private void addQualificationCard(qualification q) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.itemqualification, qualificationsContainer, false);

        TextView qualificationText = cardView.findViewById(R.id.qualificationText);
        MaterialButton removeButton = cardView.findViewById(R.id.removeButton);

        // Build qualification display text
        StringBuilder displayText = new StringBuilder();

        // Primary line: Qualification name (bold in the layout)
        if (q.qualificationName != null && !q.qualificationName.isEmpty()) {
            displayText.append(q.qualificationName);
        }

        // Secondary line: Institution and type
        if (q.institution != null && !q.institution.isEmpty()) {
            if (displayText.length() > 0) displayText.append("\n");
            displayText.append(q.institution);

            if (q.qualificationType != null && !q.qualificationType.isEmpty()
                    && !q.qualificationType.equals("Other")) {
                displayText.append(" • ").append(q.qualificationType);
            }
        }

        // Field of study
        if (q.fieldOfStudy != null && !q.fieldOfStudy.isEmpty()) {
            if (displayText.length() > 0) displayText.append("\n");
            displayText.append("Field: ").append(q.fieldOfStudy);
        }

        // Date range
        if (q.startDate != null || q.endDate != null) {
            if (displayText.length() > 0) displayText.append("\n");
            if (q.startDate != null) {
                displayText.append(q.startDate);
            }
            if (q.endDate != null) {
                displayText.append(" - ").append(q.endDate);
            } else if (q.isCurrent) {
                displayText.append(" - Present");
            }
        }

        // Grade
        if (q.gradeOrGpa != null && !q.gradeOrGpa.isEmpty()) {
            displayText.append("\nGrade: ").append(q.gradeOrGpa);
        }

        // Description (truncated if too long)
        if (q.description != null && !q.description.isEmpty()) {
            displayText.append("\n").append(q.description);
        }

        qualificationText.setText(displayText.toString());

        // Set up remove button
        removeButton.setOnClickListener(v -> confirmDelete(q, cardView));

        qualificationsContainer.addView(cardView);
    }

    private void addQualification() {
        // Clear previous errors
        clearErrors();

        // Get all field values
        String qualName = qualificationNameInput != null && qualificationNameInput.getText() != null
                ? qualificationNameInput.getText().toString().trim()
                : "";

        String qualType = qualificationTypeInput != null && qualificationTypeInput.getText() != null
                ? qualificationTypeInput.getText().toString().trim()
                : "Other";

        String institution = institutionInput != null && institutionInput.getText() != null
                ? institutionInput.getText().toString().trim()
                : "";

        String fieldOfStudy = fieldOfStudyInput != null && fieldOfStudyInput.getText() != null
                ? fieldOfStudyInput.getText().toString().trim()
                : "";

        String startDate = startDateInput != null && startDateInput.getText() != null
                ? startDateInput.getText().toString().trim()
                : null;

        String endDate = endDateInput != null && endDateInput.getText() != null
                ? endDateInput.getText().toString().trim()
                : null;

        if (endDate != null && endDate.equals("Present")) {
            endDate = null; // Will be handled by isCurrent flag
        }

        String grade = gradeInput != null && gradeInput.getText() != null
                ? gradeInput.getText().toString().trim()
                : null;

        String description = descriptionInput != null && descriptionInput.getText() != null
                ? descriptionInput.getText().toString().trim()
                : null;

        boolean isCurrent = currentlyStudyingCheckbox != null && currentlyStudyingCheckbox.isChecked();

        // Validation
        boolean isValid = true;

        if (qualName.isEmpty()) {
            if (qualificationNameLayout != null) {
                qualificationNameLayout.setError("Qualification name is required");
            }
            isValid = false;
        }

        // Optional: Validate that if start date is provided, end date should be after start date
        if (startDate != null && endDate != null && !isCurrent) {
            try {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                start.setTime(dateFormat.parse(startDate));
                end.setTime(dateFormat.parse(endDate));

                if (end.before(start)) {
                    if (endDateLayout != null) {
                        endDateLayout.setError("End date must be after start date");
                    }
                    isValid = false;
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create qualification input with all fields
        qualificationInput input = new qualificationInput();
        input.qualificationName = qualName;
        input.qualificationType = qualType;
        input.institution = institution.isEmpty() ? null : institution;
        input.fieldOfStudy = fieldOfStudy.isEmpty() ? null : fieldOfStudy;
        input.startDate = startDate;
        input.endDate = endDate;
        input.isCurrent = isCurrent;
        input.gradeOrGpa = grade;
        input.description = description;

        // Disable buttons while loading
        if (addButton != null) {
            addButton.setEnabled(false);
            addButton.setText("Adding...");
        }
        if (clearButton != null) {
            clearButton.setEnabled(false);
        }

        Call<qualification> call = api.addQualification(userId, input, apiConfig.tokenQualAdd);

        call.enqueue(new Callback<qualification>() {
            @Override
            public void onResponse(Call<qualification> call, Response<qualification> response) {
                if (addButton != null) {
                    addButton.setEnabled(true);
                    addButton.setText("Add");
                }
                if (clearButton != null) {
                    clearButton.setEnabled(true);
                }

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(settingsqualifications.this,
                            "✓ Qualification added successfully", Toast.LENGTH_SHORT).show();

                    // Clear form
                    clearForm();

                    // Reload qualifications
                    loadQualifications();
                } else {
                    Toast.makeText(settingsqualifications.this,
                            "Failed to add qualification", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<qualification> call, Throwable t) {
                if (addButton != null) {
                    addButton.setEnabled(true);
                    addButton.setText("Add");
                }
                if (clearButton != null) {
                    clearButton.setEnabled(true);
                }
                Toast.makeText(settingsqualifications.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(qualification q, View cardView) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Qualification")
                .setMessage("Are you sure you want to remove this qualification?")
                .setPositiveButton("Remove", (dialog, which) -> deleteQualification(q, cardView))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteQualification(qualification q, View cardView) {
        Call<apiResponse> call = api.deleteQualification(
                userId,
                q.qualificationId,
                apiConfig.tokenQualDelete
        );

        call.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(Call<apiResponse> call, Response<apiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(settingsqualifications.this,
                            "✓ Qualification removed", Toast.LENGTH_SHORT).show();

                    // Remove the card view with animation
                    cardView.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> {
                                qualificationsContainer.removeView(cardView);

                                // Update count in label
                                int count = qualificationsContainer.getChildCount();
                                if (qualificationsLabel != null) {
                                    if (count > 0) {
                                        qualificationsLabel.setText("Your Qualifications (" + count + ")");
                                    } else {
                                        showEmptyState();
                                    }
                                }
                            })
                            .start();
                } else {
                    Toast.makeText(settingsqualifications.this,
                            "Failed to remove qualification", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<apiResponse> call, Throwable t) {
                Toast.makeText(settingsqualifications.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, authentication.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}