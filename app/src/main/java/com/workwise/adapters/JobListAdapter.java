package com.workwise.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.workwise.R;
import com.workwise.jobs.JobDetailActivity;
import com.workwise.models.JobListingResponse;

import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.ViewHolder> {

    private List<JobListingResponse> jobs;
    private Context context;

    public JobListAdapter(Context context, List<JobListingResponse> jobs) {
        this.context = context;
        this.jobs = jobs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.job_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JobListingResponse job = jobs.get(position);

        holder.jobTitle.setText(job.jobTitle);
        holder.jobCompany.setText(job.company);
        holder.jobLocation.setText(job.location);
        holder.jobDescription.setText(job.description);

        if (job.salary > 0) {
            holder.jobSalary.setText("R " + String.format("%.0f", job.salary / 1000) + "k");
        } else {
            holder.jobSalary.setText("Negotiable");
        }

        holder.employmentTypeChip.setText(job.employmentType != null ? job.employmentType : "Full-Time");
        holder.workArrangementChip.setText(job.workArrangement != null ? job.workArrangement : "On-Site");

        holder.postedDate.setText("Posted " + getTimeDifference(job.postedDate));
        holder.applicantCount.setText(job.applicants + " applied");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("job_id", job.jobId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public void updateJobs(List<JobListingResponse> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    private String getTimeDifference(String postedDate) {
        if (postedDate == null) return "recently";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date posted = sdf.parse(postedDate);
            java.util.Date now = new java.util.Date();
            long diffMs = now.getTime() - posted.getTime();
            long diffDays = diffMs / (24 * 60 * 60 * 1000);

            if (diffDays == 0) return "today";
            else if (diffDays == 1) return "yesterday";
            else if (diffDays < 7) return diffDays + " days ago";
            else if (diffDays < 30) return (diffDays / 7) + " weeks ago";
            else return (diffDays / 30) + " months ago";
        } catch (Exception e) {
            return "recently";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobCompany, jobLocation, jobDescription;
        TextView jobSalary, postedDate, applicantCount;
        Chip employmentTypeChip, workArrangementChip;

        ViewHolder(View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobCompany = itemView.findViewById(R.id.jobCompany);
            jobLocation = itemView.findViewById(R.id.jobLocation);
            jobDescription = itemView.findViewById(R.id.jobDescription);
            jobSalary = itemView.findViewById(R.id.jobSalary);
            postedDate = itemView.findViewById(R.id.postedDate);
            applicantCount = itemView.findViewById(R.id.applicantCount);
            employmentTypeChip = itemView.findViewById(R.id.employmentTypeChip);
            workArrangementChip = itemView.findViewById(R.id.workArrangementChip);
        }
    }
}

