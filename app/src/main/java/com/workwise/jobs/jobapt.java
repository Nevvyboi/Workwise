package com.workwise.jobs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.workwise.R;
import com.workwise.models.apiResponse;
import com.workwise.models.job;
import com.workwise.models.savedJobInput;
import com.workwise.models.savedJobs;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class jobapt extends RecyclerView.Adapter<jobapt.JobViewHolder> {

    private final List<job> jobs;
    private final Map<Integer, Float> jobDistances;
    private final Context context;
    private final SharedPreferences prefs;
    private final int userId;
    private final apiService api;

    // --- START FIX ---
    // This is a workaround for the backend design.
    // We cannot link by JobID, so we link by a composite key of "Title+Company".
    // Map<"Title+Company", SavedJobID>
    private final Map<String, Integer> savedJobMap = new HashMap<>();
    // --- END FIX ---

    public jobapt(List<job> jobs, Map<Integer, Float> jobDistances, Context context) {
        this.jobs = jobs;
        this.jobDistances = jobDistances;
        this.context = context;
        this.prefs = context.getSharedPreferences("WorkWisePrefs", Context.MODE_PRIVATE);
        this.userId = prefs.getInt("user_id", -1);
        this.api = apiClient.get().create(apiService.class);

        loadSavedJobMap();
    }

    public void updateJobs(List<job> newJobs) {
        this.jobs.clear();
        this.jobs.addAll(newJobs);
        loadSavedJobMap();
    }

    /**
     * Creates a unique key for a job based on its title and company.
     * This is used to map saved jobs since the backend doesn't link them by JobID.
     */
    private String getJobKey(String title, String company) {
        // Use "::" as a separator to prevent "TitleA" + "CompanyB" being the same as "Title" + "ACompanyB"
        return (title != null ? title : "") + "::" + (company != null ? company : "");
    }

    private String getJobKey(job job) {
        return getJobKey(job.getJobTitle(), job.getCompanyName());
    }

    private String getJobKey(savedJobs savedJob) {
        // *** ASSUMPTION ***
        // This assumes your 'savedJobs' model has getJobTitle() and getCompanyName()
        // If these methods are named differently, you MUST change them here.
        return getJobKey(savedJob.getJobTitle(), savedJob.getCompanyName());
    }

    /**
     * Fetches the user's saved jobs and populates the savedJobMap.
     */
    private void loadSavedJobMap() {
        if (userId == -1) return; // Not logged in

        api.getSavedJobs(userId, apiConfig.tokenSavedList).enqueue(new Callback<List<savedJobs>>() {
            @Override
            public void onResponse(@NonNull Call<List<savedJobs>> call, @NonNull Response<List<savedJobs>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedJobMap.clear();
                    for (savedJobs savedJob : response.body()) {
                        // --- START FIX (Line 87, 88) ---
                        // We use the composite key instead of the non-existent getJobId()
                        // *** ASSUMPTION ***
                        // This assumes your 'savedJobs' model has getSavedJobId()
                        // If your model has different method names, change them here.
                        try {
                            String key = getJobKey(savedJob);
                            savedJobMap.put(key, savedJob.getSavedJobId());
                        } catch (Exception e) {
                            Log.e("jobapt", "Error processing saved job. Does it have getJobTitle() and getSavedJobId()?", e);
                        }
                        // --- END FIX ---
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<savedJobs>> call, @NonNull Throwable t) {
                Log.e("jobapt", "Failed to load saved jobs map: " + t.getMessage());
            }
        });
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

        // Your new job.java file fixes these two lines:
        holder.jobTitle.setText(job.getJobTitle());
        holder.companyName.setText(job.getCompanyName());
        holder.jobLocation.setText(job.getJobLocation() != null ? job.getJobLocation() : "N/A");
        holder.salaryRange.setText(job.getSalaryRange() != null ? job.getSalaryRange() : "N/A");

        String employmentType = job.getEmploymentType();
        if (employmentType != null && !employmentType.isEmpty()) {
            holder.jobType.setText(employmentType);
            holder.jobTypeCard.setVisibility(View.VISIBLE);
        } else {
            holder.jobTypeCard.setVisibility(View.GONE);
        }

        String workArrangement = job.getWorkArrangement();
        if (workArrangement != null && !workArrangement.isEmpty()) {
            holder.workArrangement.setText(workArrangement);
            holder.workArrangementCard.setVisibility(View.VISIBLE);
        } else {
            holder.workArrangementCard.setVisibility(View.GONE);
        }

        String postedTime = job.getDatePosted();
        if(postedTime != null && !postedTime.isEmpty() && postedTime.length() >= 10) {
            holder.postedTime.setText(postedTime.substring(0, 10));
        } else {
            holder.postedTime.setText("Recently");
        }

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

        // --- START FIX ---
        // We now check for the job's key in the map
        String jobKey = getJobKey(job);
        boolean isSaved = savedJobMap.containsKey(jobKey);
        updateBookmarkIcon(holder.bookmarkButton, isSaved);
        // --- END FIX ---

        holder.bookmarkButton.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(context, "Please login to save jobs", Toast.LENGTH_SHORT).show();
                return;
            }

            // We use the same key to check
            String key = getJobKey(job);
            boolean currentlySaved = savedJobMap.containsKey(key);

            if (currentlySaved) {
                Integer savedJobId = savedJobMap.get(key);
                if (savedJobId != null) {
                    unsaveJob(key, savedJobId, holder.bookmarkButton);
                }
            } else {
                saveJob(job, holder.bookmarkButton);
            }
        });
    }

    private void saveJob(job job, ImageView bookmarkButton) {

        // --- START FIX (Line 196) ---
        // The constructor only takes 5 arguments, so we only pass 5.
        savedJobInput input = new savedJobInput(
                job.getJobTitle(),
                job.getCompanyName(),
                job.getJobLocation(),
                job.getSalaryRange(),
                job.getDescription()
        );
        // --- END FIX ---

        Call<savedJobs> call = api.addSavedJob(userId, input, apiConfig.tokenSavedAdd);

        call.enqueue(new Callback<savedJobs>() {
            @Override
            public void onResponse(@NonNull Call<savedJobs> call, @NonNull Response<savedJobs> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedJobs newSavedJob = response.body();

                    // --- START FIX (Line 208) ---
                    // We can't use newSavedJob.getJobId(), so we build the key
                    // from the new object's title and company.
                    // *** ASSUMPTION ***
                    // Assumes newSavedJob has getJobTitle(), getCompanyName(), getSavedJobId()
                    try {
                        String key = getJobKey(newSavedJob);
                        savedJobMap.put(key, newSavedJob.getSavedJobId());
                        updateBookmarkIcon(bookmarkButton, true);
                        Toast.makeText(context, "Job saved!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Failed to save job (response error)", Toast.LENGTH_SHORT).show();
                        Log.e("jobapt", "Error processing save response. Does savedJobs model have correct getters?", e);
                    }
                    // --- END FIX ---

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

    private void unsaveJob(String jobKey, int savedJobId, ImageView bookmarkButton) {
        Call<apiResponse> call = api.deleteSavedJob(userId, savedJobId, apiConfig.tokenSavedDelete);

        call.enqueue(new Callback<apiResponse>() {
            @Override
            public void onResponse(@NonNull Call<apiResponse> call, @NonNull Response<apiResponse> response) {
                if (response.isSuccessful()) {
                    // Remove the job from our map using its key
                    savedJobMap.remove(jobKey);
                    updateBookmarkIcon(bookmarkButton, false);
                    Toast.makeText(context, "Job removed from saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove job", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<apiResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            workArrangement = itemView.findViewById(R.id.tv_work_arrangement);
            workArrangementCard = itemView.findViewById(R.id.workArrangementCard);
            distanceCard = itemView.findViewById(R.id.distanceCard);
            jobTypeCard = itemView.findViewById(R.id.jobTypeRow).findViewById(R.id.jobTypeCard);
        }
    }
}