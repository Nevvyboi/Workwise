package com.workwise;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.workwise.adapters.AssessmentHistoryAdapter;
import com.workwise.models.AssessmentHistory;

import java.util.ArrayList;
import java.util.List;

public class AssessmentActivity extends AppCompatActivity {

    private RecyclerView historyRecycler;
    private AssessmentHistoryAdapter adapter;
    private MaterialButton startAssessmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        historyRecycler = findViewById(R.id.historyRecycler);
        startAssessmentButton = findViewById(R.id.startAssessmentButton);

        adapter = new AssessmentHistoryAdapter(new ArrayList<>());
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(adapter);

        loadHistory();
        startAssessmentButton.setOnClickListener(v -> startNewAssessment());
    }

    private void loadHistory() {
        List<AssessmentHistory> list = new ArrayList<>();
        list.add(new AssessmentHistory("2025-01-15", "Technical Skills", 78));
        list.add(new AssessmentHistory("2025-01-10", "Communication", 85));
        adapter.update(list);
    }

    private void startNewAssessment() {
        Toast.makeText(this, "Assessment flow coming soon", Toast.LENGTH_SHORT).show();
    }
}
