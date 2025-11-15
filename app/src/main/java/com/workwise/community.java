package com.workwise;

import com.workwise.ui.bottomNav;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

public class community extends bottomNav {

    private TabLayout tabLayout;
    private EditText searchInput;
    private MaterialCardView chatItem1, chatItem2, chatItem3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);

        initializeViews();
        setupTabLayout();
        setupSearchBar();
        setupChatItems();
    }

    @Override
    protected String getCurrentNavItem() {
        return "community";
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        searchInput = findViewById(R.id.searchInput);
        chatItem1 = findViewById(R.id.chatItem1);
        chatItem2 = findViewById(R.id.chatItem2);
        chatItem3 = findViewById(R.id.chatItem3);

        // Top bar buttons
        findViewById(R.id.menuButton).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Menu...", Toast.LENGTH_SHORT).show();

        });

        findViewById(R.id.profileButton).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();

        });
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        // Groups tab
                        Toast.makeText(community.this, "Groups", Toast.LENGTH_SHORT).show();

                        break;
                    case 1:
                        // Unions tab
                        Toast.makeText(community.this, "Unions", Toast.LENGTH_SHORT).show();

                        break;
                    case 2:
                        // Chats tab (default)
                        Toast.makeText(community.this, "Chats", Toast.LENGTH_SHORT).show();

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        // Set default tab to "Chats" (index 2)
        TabLayout.Tab chatsTab = tabLayout.getTabAt(2);
        if (chatsTab != null) {
            chatsTab.select();
        }
    }

    private void setupSearchBar() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void setupChatItems() {
        if (chatItem1 != null) {
            chatItem1.setOnClickListener(v -> {
                Toast.makeText(this, "Opening chat with Phumlani Nkosi", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to chat screen
                // Intent intent = new Intent(this, ChatActivity.class);
                // intent.putExtra("chat_id", "1");
                // intent.putExtra("chat_name", "Phumlani Nkosi");
                // startActivity(intent);
            });
        }

        // Chat Item 2 - WorkWise Team
        if (chatItem2 != null) {
            chatItem2.setOnClickListener(v -> {
                Toast.makeText(this, "Opening WorkWise Team chat", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to chat screen
            });
        }

        // Chat Item 3 - Union SA
        if (chatItem3 != null) {
            chatItem3.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Union SA chat", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to chat screen
            });
        }
    }

    private void filterChats(String query) {
        // TODO: Implement search/filter logic
        if (query.isEmpty()) {
            // Show all chats
            showAllChats();
        } else {
            // Filter chats based on query
            // This is a simple example - you'd implement actual filtering
            Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllChats() {
        // Show all chat items
        // In a real app, you'd use RecyclerView and filter the adapter
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh chat list when returning to this screen
        // TODO: Load latest messages
    }
}