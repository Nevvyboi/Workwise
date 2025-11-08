package com.workwise.models;

public class job {
    private int jobId;
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String salaryRange;
    private String jobDescription;
    private String employmentType;
    private String postedAt;

    public int getJobId() {
        return jobId;
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

    public String getEmploymentType() {
        return employmentType;
    }

    public String getPostedAt() {
        return postedAt;
    }
}
