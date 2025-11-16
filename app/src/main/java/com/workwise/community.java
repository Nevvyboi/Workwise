package com.workwise;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.workwise.chat.chatActivity;
import com.workwise.ui.bottomNav;

public class community extends bottomNav {

    private MaterialCardView mainGroupChat;
    private MaterialButton joinChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);

        initializeViews();
        setupClicks();
    }

    @Override
    protected String getCurrentNavItem() {
        return "community";
    }

    private void initializeViews() {
        mainGroupChat = findViewById(R.id.mainGroupChat);
        joinChatButton = findViewById(R.id.joinChatButton);

        View profileBtn = findViewById(R.id.profileButton);
        if (profileBtn != null) {
            profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(community.this, "Opening Profile.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupClicks() {
        // Open the main community chat (conversationId = 1; change if needed)
        if (joinChatButton != null) {
            joinChatButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    chatActivity.open(community.this, 1, "WorkWise Community");
                }
            });
        }

        if (mainGroupChat != null) {
            mainGroupChat.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    chatActivity.open(community.this, 1, "WorkWise Community");
                }
            });
        }
    }
}
