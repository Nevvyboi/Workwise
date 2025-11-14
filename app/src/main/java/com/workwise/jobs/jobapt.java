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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.workwise.R;
import com.workwise.models.job;
import com.workwise.models.savedJobInput;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.models.savedJobs;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        loadSavedJobIds();
    }

    // Method to update the job list (for search)
    public void updateJobs(List<job> newJobs) {
        this.jobs.clear();
        this.jobs.addAll(newJobs);
        notifyDataSetChanged();
    }

    private void loadSavedJobIds() {
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
        if (job == null) return;

        holder.jobTitle.setText(job.getJobTitle());
        holder.companyName.setText(job.getCompanyName());
        holder.jobLocation.setText(job.getJobLocation() != null ? job.getJobLocation() : "N/A");
        holder.salaryRange.setText(job.getSalaryRange() != null ? job.getSalaryRange() : "N/A");

        // Bind Employment Type
        String employmentType = job.getEmploymentType();
        if (employmentType != null && !employmentType.isEmpty()) {
            holder.jobType.setText(employmentType);
            holder.jobTypeCard.setVisibility(View.VISIBLE);
        } else {
            holder.jobTypeCard.setVisibility(View.GONE);
        }

        // Bind Work Arrangement
        String workArrangement = job.getWorkArrangement();
        if (workArrangement != null && !workArrangement.isEmpty()) {
            holder.workArrangement.setText(workArrangement);
            holder.workArrangementCard.setVisibility(View.VISIBLE);
        } else {
            holder.workArrangementCard.setVisibility(View.GONE);
        }

        // Bind Posted Time (FIXED: was getPostedAt)
        String postedTime = job.getDatePosted();
        if(postedTime != null && !postedTime.isEmpty()) {
            // This is a basic format, you can add a "time ago" function later
            holder.postedTime.setText(postedTime.substring(0, 10));
        } else {
            holder.postedTime.setText("Recently");
        }


        // Bind distance
        Float distance = jobDistances.get(job.getJobId());
        if (distance != null) {
            if (distance < 1.0f) {
                holder.distance.setText(String.format(Locale.getDefault(), "%.0f m", distance * 1000));
            } else if (distance < 10.0f) {
                holder.distance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
            } else {
                holder.distance.setText(String.format(Locale.getDefault(), "%.0f km", distance));
            }
            holder.distanceCard.setVisibility(View.VISIBLE);
        } else {
            holder.distanceCard.setVisibility(View.GONE);
        }

        // Set bookmark icon state
        boolean isSaved = savedJobIds.contains(job.getJobId());
        updateBookmarkIcon(holder.bookmarkButton, isSaved);

        holder.bookmarkButton.setOnClickListener(v -> {
            boolean currentlySaved = savedJobIds.contains(job.getJobId());
            if (currentlySaved) {
                unsaveJob(job, holder.bookmarkButton);
            } else {
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

        savedJobInput input = new savedJobInput(
                job.getJobTitle(),
                job.getCompanyName(),
                job.getJobLocation(),
                job.getSalaryRange(),
                job.getDescription()
        );

        apiService api = apiClient.get().create(apiService.class);
        Call<savedJobs> call = api.addSavedJob(userId, input, apiConfig.tokenSavedAdd);

        call.enqueue(new Callback<savedJobs>() {
            @Override
            public void onResponse(@NonNull Call<savedJobs> call, @NonNull Response<savedJobs> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedJobIds.add(job.getJobId());
                    saveSavedJobIds();
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
        if (userId == -1) return;

        // Note: This only removes locally. You need a "deleteSavedJob" API call
        // that takes the 'job.getJobId()' to properly delete from server.
        savedJobIds.remove(job.getJobId());
        saveSavedJobIds();
        updateBookmarkIcon(bookmarkButton, false);
        Toast.makeText(context, "Job removed from saved", Toast.LENGTH_SHORT).show();
    }

    private void updateBookmarkIcon(ImageView bookmarkButton, boolean isSaved) {
        if (isSaved) {
            bookmarkButton.setImageResource(R.drawable.baseline_bookmark_24);
            bookmarkButton.setColorFilter(context.getResources().getColor(R.color.colorPrimary, null));
        } else {
            bookmarkButton.setImageResource(R.drawable.outline_bookmark_border_24);
            bookmarkButton.setColorFilter(context.getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, companyName, jobLocation, salaryRange, jobType, postedTime, distance;
        ImageView bookmarkButton;
        TextView workArrangement;
        MaterialCardView workArrangementCard, distanceCard, jobTypeCard;

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

            // Bind new views
            workArrangement = itemView.findViewById(R.id.tv_work_arrangement);
            workArrangementCard = itemView.findViewById(R.id.workArrangementCard);
            distanceCard = itemView.findViewById(R.id.distanceCard);
            jobTypeCard = itemView.findViewById(R.id.jobTypeRow).findViewById(R.id.jobTypeCard);
        }
    }
}