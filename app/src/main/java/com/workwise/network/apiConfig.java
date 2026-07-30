package com.workwise.network;

import com.workwise.BuildConfig;

/**
 * Where the backend lives and how we authenticate to it.
 *
 * Nothing in here is a literal. The base URL and the shared token come from
 * BuildConfig, which build.gradle.kts fills in from local.properties (or the
 * matching environment variables). local.properties is gitignored, so no
 * secret is ever committed. See local.properties.example.
 *
 * Every protected route is guarded by the same shared token, sent as an
 * X-Endpoint-Token header. It has to match WORKWISE_ENDPOINT_TOKEN on the
 * WorkwiseWeb backend.
 */
public final class apiConfig {

    /** Backend base URL. Defaults to the host machine as seen from the emulator. */
    public static final String baseUrl = BuildConfig.API_BASE_URL;

    /** Shared endpoint token. Empty until local.properties has been filled in. */
    public static final String endpointToken = BuildConfig.API_TOKEN;

    // The call sites below were each written against their own constant. They
    // all resolve to the one token now, which leaves those call sites untouched
    // while there is only a single secret to configure.
    public static final String tokenRegister = endpointToken;
    public static final String tokenLogin = endpointToken;

    public static final String tokenProfileGet = endpointToken;
    public static final String tokenProfileUpdate = endpointToken;
    public static final String tokenProfileImage = endpointToken;

    public static final String tokenCvList = endpointToken;
    public static final String tokenCvUpload = endpointToken;
    public static final String tokenCvDelete = endpointToken;
    public static final String tokenCvPrimary = endpointToken;

    public static final String tokenQualList = endpointToken;
    public static final String tokenQualAdd = endpointToken;
    public static final String tokenQualUpdate = endpointToken;
    public static final String tokenQualDelete = endpointToken;

    public static final String tokenStats = endpointToken;

    public static final String tokenJobsList = endpointToken;
    public static final String tokenJobSearch = endpointToken;

    public static final String tokenSavedList = endpointToken;
    public static final String tokenSavedAdd = endpointToken;
    public static final String tokenSavedDelete = endpointToken;

    public static final String tokenForgotPassword = endpointToken;
    public static final String tokenVerifyResetCode = endpointToken;
    public static final String tokenResetPassword = endpointToken;

    public static final String tokenChatCreate = endpointToken;
    public static final String tokenChatList = endpointToken;
    public static final String tokenChatMsgList = endpointToken;
    public static final String tokenChatMsgSend = endpointToken;

    /** ws:// or wss:// equivalent of the base URL, for the chat socket. */
    public static String getWssBase() {
        if (baseUrl.startsWith("https://")) return "wss://" + baseUrl.substring("https://".length());
        if (baseUrl.startsWith("http://")) return "ws://" + baseUrl.substring("http://".length());
        return baseUrl;
    }

    private apiConfig() {}
}
