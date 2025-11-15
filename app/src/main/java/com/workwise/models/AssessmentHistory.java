package com.workwise.models;

public class AssessmentHistory {
    public String date;
    public String category;
    public int score;

    public AssessmentHistory(String date, String category, int score) {
        this.date = date;
        this.category = category;
        this.score = score;
    }
}
