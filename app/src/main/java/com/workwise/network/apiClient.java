package com.workwise.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class apiClient {

    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;
    private static com.workwise.network.apiService service;

    private apiClient() {}

    private static OkHttpClient http() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)                 // remove if you don’t want verbose logs
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)        // keep WS open indefinitely
                    .pingInterval(20, TimeUnit.SECONDS)      // keep-alive pings for WS
                    .build();
        }
        return okHttpClient;
    }

    public static Retrofit get() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiConfig.baseUrl)              // e.g. https://your.api
                    .client(http())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static com.workwise.network.apiService service() {
        if (service == null) {
            service = get().create(com.workwise.network.apiService.class);
        }
        return service;
    }

    public static WebSocket openChatSocket(int conversationId, int userId, WebSocketListener listener) {
        String wsUrl = apiConfig.getWssBase()
                + "/v1/workwise/ws/chat"
                + "?conversation_id=" + conversationId
                + "&user_id=" + userId;

        Request.Builder rb = new Request.Builder().url(wsUrl);

        Request req = rb.build();
        return http().newWebSocket(req, listener);
    }

    /** Optional: tidy up resources if you ever need to fully tear down the client */
    public static void shutdown() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
        }
    }
}
