package com.workwise.models;

import com.google.gson.annotations.SerializedName;

public class job {

    @SerializedName("jobId")
    private int jobId;

    @SerializedName("jobTitle")
    private String jobTitle;

    @SerializedName("description")
    private String description; // Added for saveJob function

    @SerializedName("businessName")
    private String businessName; // This is the field you were missing

    @SerializedName("location")
    private String location; // This is the field you were missing

    @SerializedName("salaryRange")
    private String salaryRange;

    @SerializedName("employmentType")
    private String employmentType; // e.g., "Full-time"

    @SerializedName("workArrangement")
    private String workArrangement; // e.g., "Hybrid"

    @SerializedName("datePosted")
    private String datePosted;

    // --- Getters ---

    public int getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public String getDescription() { return description; }

    // These methods now exist and will fix your errors
    public String getCompanyName() { return businessName; }
    public String getJobLocation() { return location; }

    public String getSalaryRange() { return salaryRange; }
    public String getEmploymentType() { return employmentType; }
    public String getWorkArrangement() { return workArrangement; }
    public String getDatePosted() { return datePosted; } // Use this, not getPostedAt()
}