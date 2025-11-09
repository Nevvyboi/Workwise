package com.workwise.models;

public class resetPasswordIn {
    public String email;
    public String resetCode;
    public String newPassword;

    public resetPasswordIn(String email, String resetCode, String newPassword) {
        this.email = email;
        this.resetCode = resetCode;
        this.newPassword = newPassword;
    }
}