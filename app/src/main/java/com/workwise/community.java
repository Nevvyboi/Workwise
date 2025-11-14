package com.workwise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.workwise.R;
import com.workwise.models.ConversationCreateIn;
import com.workwise.models.ConversationOut;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;
import com.workwise.ui.bottomNav;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class community extends bottomNav {

    private MaterialButton joinChatButton;
    private apiService svc;
    private static final String PREF_NAME = "WorkWisePrefs";
    private static final int COMMUNITY_CHAT_ID = 1000; // Fixed community chat ID

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);

        // Initialize views
        joinChatButton = findViewById(R.id.joinChatButton);

        // API service
        svc = apiClient.service();

        // Set click listener for join chat button
        if (joinChatButton != null) {
            joinChatButton.setOnClickListener(v -> joinCommunityChat());
        }
    }

    @Override
    protected String getCurrentNavItem() {
        return "community";
    }

    private void joinCommunityChat() {
        int me = getCurrentUserId();
        if (me <= 0) {
            toast("Please sign in again");
            Log.e("COMMUNITY", "Invalid user ID: " + me);
            return;
        }

        Log.d("COMMUNITY", "Joining community chat, user ID: " + me);

        // Use fixed community chat ID instead of creating new one
        List<Integer> participants = Arrays.asList(me, COMMUNITY_CHAT_ID);

        svc.createChat(apiConfig.tokenChatCreate, new ConversationCreateIn(participants))
                .enqueue(new Callback<ConversationOut>() {
                    @Override
                    public void onResponse(Call<ConversationOut> call, Response<ConversationOut> resp) {
                        Log.d("COMMUNITY", "Response code: " + resp.code());
                        if (!resp.isSuccessful() || resp.body() == null) {
                            String errorMsg = "Failed to join community chat (" + resp.code() + ")";
                            toast(errorMsg);
                            Log.e("COMMUNITY", errorMsg);
                            // Fallback: open chat directly with community ID
                            launchChatActivity(COMMUNITY_CHAT_ID, "WorkWise Community");
                            return;
                        }
                        Log.d("COMMUNITY", "Community chat joined with ID: " + resp.body().conversationId);
                        launchChatActivity(resp.body().conversationId, "WorkWise Community");
                    }

                    @Override
                    public void onFailure(Call<ConversationOut> call, Throwable t) {
                        String errorMsg = "Network error: " + t.getMessage();
                        toast(errorMsg);
                        Log.e("COMMUNITY", errorMsg);
                        // Fallback: open chat directly
                        launchChatActivity(COMMUNITY_CHAT_ID, "WorkWise Community");
                    }
                });
    }

    private void launchChatActivity(int conversationId, String displayName) {
        Intent i = new Intent(this, com.workwise.chat.chatActivity.class);
        i.putExtra("conversationId", conversationId);
        i.putExtra("displayName", displayName);
        startActivity(i);
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}