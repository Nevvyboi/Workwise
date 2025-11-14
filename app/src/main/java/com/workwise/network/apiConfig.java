package com.workwise.network;

public final class apiConfig {

    public static final String baseUrl = "http://192.168.1.158:8000/";

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

    public static final String tokenChatCreate   = "CHATCREATETOK111";
    public static final String tokenChatList     = "CHATLISTTOK222";
    public static final String tokenChatMsgList  = "CHATMSGLISTTOK333";
    public static final String tokenChatMsgSend  = "CHATMSGSENDTOK444";

    public static String getWssBase() {
        if (baseUrl.startsWith("https://")) return "wss://" + baseUrl.substring("https://".length());
        if (baseUrl.startsWith("http://"))  return "ws://"  + baseUrl.substring("http://".length());
        return baseUrl; // fallback if already ws(s)
    }

    private apiConfig() {}
}