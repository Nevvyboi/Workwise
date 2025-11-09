package com.workwise.models;

public class savedJobInput {
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String salaryRange;
    private String jobDescription;

    public savedJobInput(String jobTitle, String companyName, String jobLocation,
                         String salaryRange, String jobDescription) {
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobLocation = jobLocation;
        this.salaryRange = salaryRange;
        this.jobDescription = jobDescription;
    }

    // Getters
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
}