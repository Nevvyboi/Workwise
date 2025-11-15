package com.workwise.jobs;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.workwise.R;

public class JobDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Example: Retrieve job_id from intent
        int jobId = getIntent().getIntExtra("job_id", -1);

        // Display job_id for testing purposes
        TextView jobIdTextView = findViewById(R.id.jobIdTextView);
        jobIdTextView.setText("Job ID: " + jobId);
    }
}
