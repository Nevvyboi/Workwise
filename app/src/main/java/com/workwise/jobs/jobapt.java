package com.workwise.jobs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.workwise.R;
import com.workwise.models.job;

public class jobapt extends RecyclerView.Adapter<jobapt.JobViewHolder> {

    private final List<job> jobs;

    public jobapt(List<job> jobs) {
        this.jobs = jobs;
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
        holder.jobTitle.setText(job.getJobTitle());
        holder.companyName.setText(job.getCompanyName());

        String location = job.getJobLocation() != null ? job.getJobLocation() : "Location not specified";
        holder.jobLocation.setText(location);

        String salary = job.getSalaryRange() != null ? job.getSalaryRange() : "Salary not disclosed";
        holder.salaryRange.setText(salary);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, companyName, jobLocation, salaryRange;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.tv_job_title);
            companyName = itemView.findViewById(R.id.tv_company_name);
            jobLocation = itemView.findViewById(R.id.tv_location);
            salaryRange = itemView.findViewById(R.id.tv_salary);
        }
    }
}
