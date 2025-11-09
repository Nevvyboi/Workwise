package com.workwise.jobs;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.workwise.R;
import com.workwise.models.job;
import com.workwise.models.savedJobInput;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.models.savedJobs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class jobapt extends RecyclerView.Adapter<jobapt.JobViewHolder> {

    private final List<job> jobs;
    private final Map<Integer, Float> jobDistances;
    private final Context context;
    private final Set<Integer> savedJobIds; // Track which jobs are bookmarked
    private final SharedPreferences prefs;

    public jobapt(List<job> jobs, Map<Integer, Float> jobDistances, Context context) {
        this.jobs = jobs;
        this.jobDistances = jobDistances;
        this.context = context;
        this.savedJobIds = new HashSet<>();
        this.prefs = context.getSharedPreferences("WorkWisePrefs", Context.MODE_PRIVATE);

        // Load saved job IDs from SharedPreferences
        loadSavedJobIds();
    }

    private void loadSavedJobIds() {
        // Load the set of saved job IDs
        Set<String> savedIds = prefs.getStringSet("saved_job_ids", new HashSet<>());
        for (String id : savedIds) {
            try {
                savedJobIds.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSavedJobIds() {
        // Save the set of saved job IDs to SharedPreferences
        Set<String> savedIds = new HashSet<>();
        for (Integer id : savedJobIds) {
            savedIds.add(String.valueOf(id));
        }
        prefs.edit().putStringSet("saved_job_ids", savedIds).apply();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemjobcard, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        job job = jobs.get(position);

        // Bind job title
        holder.jobTitle.setText(job.getJobTitle());

        // Bind company name
        holder.companyName.setText(job.getCompanyName());

        // Bind location with fallback
        String location = job.getJobLocation() != null ? job.getJobLocation() : "Location not specified";
        holder.jobLocation.setText(location);

        // Bind salary with fallback
        String salary = job.getSalaryRange() != null ? job.getSalaryRange() : "Salary not disclosed";
        holder.salaryRange.setText(salary);

        // Bind employment type with fallback
        String employmentType = job.getEmploymentType() != null ? job.getEmploymentType() : "Full-Time";
        holder.jobType.setText(employmentType);

        // Bind posted time with fallback
        String postedTime = job.getPostedAt() != null ? job.getPostedAt() : "Recently";
        holder.postedTime.setText(postedTime);

        // Bind distance - get from map or show placeholder
        Float distance = jobDistances.get(job.getJobId());
        if (distance != null) {
            // Format distance nicely
            if (distance < 1.0f) {
                // Less than 1km, show in meters
                holder.distance.setText(String.format(Locale.getDefault(), "%.0f m", distance * 1000));
            } else if (distance < 10.0f) {
                // Less than 10km, show one decimal
                holder.distance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
            } else {
                // 10km or more, show whole number
                holder.distance.setText(String.format(Locale.getDefault(), "%.0f km", distance));
            }
        } else {
            holder.distance.setText("-- km");
        }

        // Set bookmark icon state
        boolean isSaved = savedJobIds.contains(job.getJobId());
        updateBookmarkIcon(holder.bookmarkButton, isSaved);

        // Handle bookmark button click
        holder.bookmarkButton.setOnClickListener(v -> {
            boolean currentlySaved = savedJobIds.contains(job.getJobId());

            if (currentlySaved) {
                // Remove from saved jobs
                unsaveJob(job, holder.bookmarkButton);
            } else {
                // Add to saved jobs
                saveJob(job, holder.bookmarkButton);
            }
        });
    }

    private void saveJob(job job, ImageView bookmarkButton) {
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(context, "Please login to save jobs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create saved job input
        savedJobInput input = new savedJobInput(
                job.getJobTitle(),
                job.getCompanyName(),
                job.getJobLocation(),
                job.getSalaryRange(),
                job.getJobDescription()
        );

        apiService api = apiClient.get().create(apiService.class);
        Call<savedJobs> call = api.addSavedJob(userId, input, apiConfig.tokenSavedAdd);

        call.enqueue(new Callback<savedJobs>() {
            @Override
            public void onResponse(@NonNull Call<savedJobs> call, @NonNull Response<savedJobs> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Add to local set
                    savedJobIds.add(job.getJobId());
                    saveSavedJobIds();

                    // Update UI
                    updateBookmarkIcon(bookmarkButton, true);
                    Toast.makeText(context, "Job saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save job", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<savedJobs> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unsaveJob(job job, ImageView bookmarkButton) {
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            return;
        }

        // For unsaving, we need the savedJobId from the backend
        // For now, we'll just remove from local storage and update UI
        // You'll need to implement proper backend deletion if needed

        savedJobIds.remove(job.getJobId());
        saveSavedJobIds();
        updateBookmarkIcon(bookmarkButton, false);
        Toast.makeText(context, "Job removed from saved", Toast.LENGTH_SHORT).show();
    }

    private void updateBookmarkIcon(ImageView bookmarkButton, boolean isSaved) {
        if (isSaved) {
            bookmarkButton.setImageResource(android.R.drawable.btn_star_big_on);
            bookmarkButton.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
        } else {
            bookmarkButton.setImageResource(android.R.drawable.btn_star_big_off);
            bookmarkButton.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, companyName, jobLocation, salaryRange, jobType, postedTime, distance;
        ImageView bookmarkButton;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.tv_job_title);
            companyName = itemView.findViewById(R.id.tv_company_name);
            jobLocation = itemView.findViewById(R.id.tv_location);
            salaryRange = itemView.findViewById(R.id.tv_salary);
            jobType = itemView.findViewById(R.id.tv_job_type);
            postedTime = itemView.findViewById(R.id.tv_posted_time);
            distance = itemView.findViewById(R.id.tv_distance);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
        }
    }
}