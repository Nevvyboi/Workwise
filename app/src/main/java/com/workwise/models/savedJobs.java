package com.workwise.models;

import com.google.gson.annotations.SerializedName;

public class savedJobs {
    @SerializedName("savedJobId")
    private int savedJobId;
    @SerializedName("userId")
    private int userId;
    @SerializedName("jobTitle")
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String salaryRange;
    private String jobDescription;
    private String savedAt;

    // Getters
    public int getSavedJobId() {
        return savedJobId;
    }

    public int getUserId() {
        return userId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getSavedAt() {
        return savedAt;
    }

    // Setters
    public void setSavedJobId(int savedJobId) {
        this.savedJobId = savedJobId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setSavedAt(String savedAt) {
        this.savedAt = savedAt;
    }
}