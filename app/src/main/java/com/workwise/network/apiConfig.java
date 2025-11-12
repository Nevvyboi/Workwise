package com.workwise.network;

public final class apiConfig {

    public static final String baseUrl = "http://172.29.32.1:8000/";

    // Authentication tokens
    public static final String tokenRegister = "USNACCTOK123";
    public static final String tokenLogin = "USNDPNQNKW";

    // Profile tokens
    public static final String tokenProfileGet = "PROFILEGETTOK456";
    public static final String tokenProfileUpdate = "PROFILEUPDATETOK789";
    public static final String tokenProfileImage = "PROFILEIMGTOK012";

    // CV tokens
    public static final String tokenCvList = "CVLISTTOK345";
    public static final String tokenCvUpload = "CVUPLOADTOK678";
    public static final String tokenCvDelete = "CVDELETETOK901";
    public static final String tokenCvPrimary = "CVPRIMARYTOK234";

    // Qualifications tokens
    public static final String tokenQualList = "QUALLISTTOK567";
    public static final String tokenQualAdd = "QUALADDTOK890";
    public static final String tokenQualUpdate = "QUALUPDATETOK123";
    public static final String tokenQualDelete = "QUALDELETETOK456";

    // Stats token
    public static final String tokenStats = "STATSTOK789";

    // Jobs tokens
    public static final String tokenJobsList = "JOBLISTTOK111";

    // Saved Jobs tokens
    public static final String tokenSavedList = "SAVEDLISTTOK012";
    public static final String tokenSavedAdd = "SAVEDADDTOK345";
    public static final String tokenSavedDelete = "SAVEDDELETETOK678";

    public static final String tokenForgotPassword = "FORGOTPWDTOK123";
    public static final String tokenVerifyResetCode = "VERIFYCODETOK456";
    public static final String tokenResetPassword = "RESETPWDTOK789";

    private apiConfig() {}
}