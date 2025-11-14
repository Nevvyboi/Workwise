package com.workwise.models;

import com.google.gson.annotations.SerializedName;

// This class can extend 'job' if you want to avoid duplicate fields,
// but for simplicity, here it is as a standalone class.
public class JobDetailResponse {

    @SerializedName("jobId")
    private int jobId;

    @SerializedName("jobTitle")
    private String jobTitle;

    @SerializedName("description")
    private String description;

    @SerializedName("requirements")
    private String requirements;

    @SerializedName("salaryRange")
    private String salaryRange;

    @SerializedName("location")
    private String location;

    @SerializedName("workArrangement")
    private String workArrangement;

    @SerializedName("employmentType")
    private String employmentType;

    @SerializedName("datePosted")
    private String datePosted;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("businessId")
    private int businessId;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("businessAddress")
    private String businessAddress;

    @SerializedName("businessWebsite")
    private String businessWebsite;

    @SerializedName("businessIndustry")
    private String businessIndustry;

    @SerializedName("businessDescription")
    private String businessDescription;

    // --- Getters ---

    public int getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public String getDescription() { return description; }
    public String getRequirements() { return requirements; }
    public String getSalaryRange() { return salaryRange; }
    public String getLocation() { return location; }
    public String getWorkArrangement() { return workArrangement; }
    public String getEmploymentType() { return employmentType; }
    public String getDatePosted() { return datePosted; }
    public boolean isActive() { return isActive; }
    public int getBusinessId() { return businessId; }
    public String getBusinessName() { return businessName; }
    public String getBusinessAddress() { return businessAddress; }
    public String getBusinessWebsite() { return businessWebsite; }
    public String getBusinessIndustry() { return businessIndustry; }
    public String getBusinessDescription() { return businessDescription; }
}