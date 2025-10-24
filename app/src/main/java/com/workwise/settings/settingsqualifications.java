package com.workwise.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.workwise.R;

import java.util.ArrayList;
import java.util.List;

public class settingsqualifications extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputEditText qualificationInput;
    private MaterialButton addButton;
    private LinearLayout qualificationsContainer;

    private List<String> qualifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsqualifications);

        initializeViews();
        setupClickListeners();
        loadQualifications();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        qualificationInput = findViewById(R.id.qualificationInput);
        addButton = findViewById(R.id.addButton);
        qualificationsContainer = findViewById(R.id.qualificationsContainer);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addButton.setOnClickListener(v -> addQualification());
    }

    private void loadQualifications() {
        // TODO: Load from SharedPreferences or database
        qualifications.add("computer science");
        displayQualifications();
    }

    private void addQualification() {
        String qualification = qualificationInput.getText().toString().trim();

        if (qualification.isEmpty()) {
            qualificationInput.setError("Please enter a qualification");
            return;
        }

        qualifications.add(qualification);
        qualificationInput.setText("");
        displayQualifications();

        Toast.makeText(this, "Qualification added", Toast.LENGTH_SHORT).show();

        // TODO: Save to SharedPreferences or database
    }

    private void displayQualifications() {
        qualificationsContainer.removeAllViews();

        for (int i = 0; i < qualifications.size(); i++) {
            final int index = i;
            final String qualification = qualifications.get(i);

            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.itemqualification, qualificationsContainer, false);

            TextView qualificationText = itemView.findViewById(R.id.qualificationText);
            MaterialButton removeButton = itemView.findViewById(R.id.removeButton);

            qualificationText.setText(qualification);
            removeButton.setOnClickListener(v -> removeQualification(index));

            qualificationsContainer.addView(itemView);
        }
    }

    private void removeQualification(int index) {
        if (index >= 0 && index < qualifications.size()) {
            qualifications.remove(index);
            displayQualifications();
            Toast.makeText(this, "Qualification removed", Toast.LENGTH_SHORT).show();

            // TODO: Save to SharedPreferences or database
        }
    }
}