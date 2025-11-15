package com.workwise.settings;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.workwise.R;
import com.workwise.adapters.SkillCategoryAdapter;
import com.workwise.adapters.AssessmentHistoryAdapter;
import com.workwise.models.SkillCategory;
import com.workwise.models.AssessmentHistory;

import java.util.ArrayList;
import java.util.List;

public class skillassessment extends AppCompatActivity {

    private ImageButton backButton;
    private MaterialButton startAssessmentBtn;
    private RecyclerView skillsRecyclerView;
    private RecyclerView historyRecyclerView;
    private SkillCategoryAdapter skillAdapter;
    private AssessmentHistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.skillassessment);

        initializeViews();
        setupClickListeners();
        loadSkillCategories();
        loadAssessmentHistory();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        startAssessmentBtn = findViewById(R.id.startAssessmentBtn);
        skillsRecyclerView = findViewById(R.id.skillsRecyclerView);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);

        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (startAssessmentBtn != null) {
            startAssessmentBtn.setOnClickListener(v -> startNewAssessment());
        }
    }

    private void loadSkillCategories() {
        List<SkillCategory> skills = new ArrayList<>();
        skills.add(new SkillCategory("Communication", "Advanced", 85));
        skills.add(new SkillCategory("Leadership", "Intermediate", 72));
        skills.add(new SkillCategory("Technical Skills", "Advanced", 88));
        skills.add(new SkillCategory("Problem Solving", "Intermediate", 75));
        skills.add(new SkillCategory("Project Management", "Beginner", 60));

        skillAdapter = new SkillCategoryAdapter(skills);
        skillsRecyclerView.setAdapter(skillAdapter);
    }

    private void loadAssessmentHistory() {
        List<AssessmentHistory> history = new ArrayList<>();
        history.add(new AssessmentHistory("Jan 15, 2025", "Technical Skills", 78));
        history.add(new AssessmentHistory("Jan 10, 2025", "Communication", 82));
        history.add(new AssessmentHistory("Jan 05, 2025", "Leadership", 70));
        history.add(new AssessmentHistory("Dec 28, 2024", "Problem Solving", 76));

        historyAdapter = new AssessmentHistoryAdapter(history);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void startNewAssessment() {
        Toast.makeText(this, "Assessment Quiz - Coming Soon!", Toast.LENGTH_LONG).show();
    }
}
