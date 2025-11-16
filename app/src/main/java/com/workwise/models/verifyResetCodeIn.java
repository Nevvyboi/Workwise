package com.workwise.models;

import com.google.gson.annotations.SerializedName;

/**
 * Must match FastAPI Pydantic model EXACTLY:
 *   email: str
 *   code:  str
 */
public class verifyResetCodeIn {

    @SerializedName("email")
    private final String email;

    @SerializedName("code")
    private final String code;

    // Constructor
    public verifyResetCodeIn(String email, String code) {
        this.email = email;
        this.code  = code;
    }

    // Getters (Retrofit needs them for serialization)
    public String getEmail() { return email; }
    public String getCode()  { return code;  }
}