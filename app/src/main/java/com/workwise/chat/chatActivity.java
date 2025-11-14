package com.workwise.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.workwise.R;
import com.workwise.models.MessageOut;
import com.workwise.models.MessageSendIn;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.util.ArrayList;
import java.util.List;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.SharedPreferences;

public class chatActivity extends AppCompatActivity {

    private int conversationId;
    private String displayName;
    private int meUserId;

    private TextView title;
    private RecyclerView list;
    private EditText input;
    private ImageButton sendBtn;

    private final List<MessageOut> items = new ArrayList<>();
    private chatAdapter adapter;

    private apiService svc;
    private WebSocket ws;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        meUserId = prefs.getInt("user_id", -1);
        if (meUserId <= 0) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        conversationId = getIntent().getIntExtra("conversationId", -1);
        displayName = getIntent().getStringExtra("displayName");

        if (conversationId <= 0) {
            Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        title = findViewById(R.id.chatTitle);
        list = findViewById(R.id.chatList);
        input = findViewById(R.id.chatInput);
        sendBtn = findViewById(R.id.chatSend);

        title.setText(displayName != null ? displayName : "Chat");

        // Setup RecyclerView
        adapter = new chatAdapter(items, meUserId);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // Retrofit service
        svc = apiClient.service();

        loadMessageHistory();

        // Setup WebSocket for real-time messages
        setupWebSocket();

        // Send button click listener
        sendBtn.setOnClickListener(v -> sendMessage());

        // Send on Enter key
        input.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessageHistory() {
        svc.getMessages(apiConfig.tokenChatMsgList, conversationId, 50, null)
                .enqueue(new Callback<List<MessageOut>>() {
                    @Override
                    public void onResponse(Call<List<MessageOut>> call, Response<List<MessageOut>> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            items.clear();
                            items.addAll(resp.body());
                            adapter.notifyDataSetChanged();
                            if (!items.isEmpty()) {
                                list.scrollToPosition(items.size() - 1);
                            }
                        } else {
                            toast("Failed to load messages (" + resp.code() + ")");
                            // Add sample messages for testing
                            addSampleMessages();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MessageOut>> call, Throwable t) {
                        toast("Network error: " + t.getMessage());
                        // Add sample messages for testing
                        addSampleMessages();
                    }
                });
    }

    private void setupWebSocket() {
        ws = apiClient.openChatSocket(conversationId, meUserId, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    MessageOut msg = gson.fromJson(text, MessageOut.class);
                    runOnUiThread(() -> {
                        items.add(msg);
                        adapter.notifyItemInserted(items.size() - 1);
                        list.scrollToPosition(items.size() - 1);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                runOnUiThread(() -> toast("Connected to chat"));
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                runOnUiThread(() -> toast("Chat disconnected"));
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                runOnUiThread(() -> toast("Connection error: " + t.getMessage()));
            }
        });
    }

    private void sendMessage() {
        String messageText = input.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Clear input immediately for better UX
        input.setText("");

        // Create message object
        MessageSendIn message = new MessageSendIn(meUserId, messageText);

        svc.sendMessage(apiConfig.tokenChatMsgSend, conversationId, message)
                .enqueue(new Callback<MessageOut>() {
                    @Override
                    public void onResponse(Call<MessageOut> call, Response<MessageOut> resp) {
                        if (!resp.isSuccessful()) {
                            toast("Failed to send message (" + resp.code() + ")");
                            // Add message locally if send fails but user sees it
                            addLocalMessage(messageText);
                        }
                        // If successful, WebSocket will handle the incoming message
                    }

                    @Override
                    public void onFailure(Call<MessageOut> call, Throwable t) {
                        toast("Send error: " + t.getMessage());
                        // Add message locally
                        addLocalMessage(messageText);
                    }
                });
    }

    private void addLocalMessage(String messageText) {
        MessageOut localMessage = new MessageOut();
        localMessage.senderId = meUserId;
        localMessage.body = messageText;
        localMessage.createdAt = "Just now";

        runOnUiThread(() -> {
            items.add(localMessage);
            adapter.notifyItemInserted(items.size() - 1);
            list.scrollToPosition(items.size() - 1);
        });
    }

    private void addSampleMessages() {
        // Add some sample messages for testing
        if (items.isEmpty()) {
            MessageOut msg1 = new MessageOut();
            msg1.senderId = (meUserId == 1012) ? 1001 : 1012; // Other user
            msg1.body = "Hello! Welcome to the chat.";
            msg1.createdAt = "10:30 AM";

            MessageOut msg2 = new MessageOut();
            msg2.senderId = meUserId;
            msg2.body = "Hi there! Thanks for connecting.";
            msg2.createdAt = "10:31 AM";

            items.add(msg1);
            items.add(msg2);

            adapter.notifyDataSetChanged();
            list.scrollToPosition(items.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ws != null) {
            ws.close(1000, "Activity destroyed");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}