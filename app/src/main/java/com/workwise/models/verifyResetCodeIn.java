package com.workwise.models;

public class verifyResetCodeIn {
    public String email;
    public String resetCode;

    public verifyResetCodeIn(String email, String resetCode) {
        this.email = email;
        this.resetCode = resetCode;
    }
}