package com.workwise.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.workwise.R;
import com.workwise.models.MessageOut;
import com.workwise.models.MessageSendIn;
import com.workwise.network.apiClient;
import com.workwise.network.apiConfig;
import com.workwise.network.apiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class chatActivity extends AppCompatActivity {

    // ===== Intent extras (keep consistent everywhere)
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_DISPLAY_NAME    = "extra_display_name";

    // Helper to open this screen from anywhere
    public static void open(Context ctx, int conversationId, String displayName) {
        Intent i = new Intent(ctx, chatActivity.class);
        i.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        i.putExtra(EXTRA_DISPLAY_NAME, displayName);
        if (!(ctx instanceof Activity)) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    // ===== Views
    private TextView titleView;
    private ImageView backBtn;
    private View loadingOverlay;
    private RecyclerView chatList;
    private EditText chatInput;
    private FloatingActionButton chatSend;

    // ===== State
    private int conversationId = -1;
    private String displayName = "Chat";
    private int meUserId = -1;

    // ===== Data
    private final List<MessageOut> items = new ArrayList<>();
    private chatAdapter adapter;

    // ===== Network
    private apiService api;
    private Call<List<MessageOut>> loadCall;
    private Call<MessageOut> sendCall;
    // IMPORTANT: your API uses different tokens for list vs send
    private String tokenChatList;
    private String tokenChatSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Read args
        Intent it = getIntent();
        if (it != null) {
            conversationId = it.getIntExtra(EXTRA_CONVERSATION_ID, -1);
            String nameArg  = it.getStringExtra(EXTRA_DISPLAY_NAME);
            if (!TextUtils.isEmpty(nameArg)) displayName = nameArg;
        }

        // Load current user id
        SharedPreferences prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        meUserId = prefs.getInt("user_id", -1);

        // Validate basics early
        if (conversationId <= 0) {
            toast("Invalid conversation");
            finish();
            return;
        }
        if (meUserId <= 0) {
            toast("Please sign in again");
            finish();
            return;
        }

        // Views
        titleView = findViewById(R.id.chatTitle);
        backBtn   = findViewById(R.id.backButton); // may be null if hidden in XML
        loadingOverlay = findViewById(R.id.loadingOverlay);
        chatList  = findViewById(R.id.chatList);
        chatInput = findViewById(R.id.chatInput);
        chatSend  = findViewById(R.id.chatSend);

        if (titleView != null) titleView.setText(displayName);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // Recycler
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatList.setLayoutManager(lm);
        adapter = new chatAdapter(items, meUserId);
        chatList.setAdapter(adapter);

        // Retrofit + tokens
        api = apiClient.service();
        tokenChatList = apiConfig.tokenChatMsgList; // GET
        tokenChatSend = apiConfig.tokenChatMsgSend; // POST

        // Load history
        fetchMessages();

        // Send click + IME send
        chatSend.setOnClickListener(v -> sendMessage());
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadCall != null) loadCall.cancel();
        if (sendCall != null) sendCall.cancel();
    }

    // ===== API actions

    private void fetchMessages() {
        showLoading(true);

        // apiService: getMessages(@Header token, @Path conversation_id, @Query limit, @Query before)
        loadCall = api.getMessages(tokenChatList, conversationId, 50, null);
        loadCall.enqueue(new Callback<List<MessageOut>>() {
            @Override
            public void onResponse(Call<List<MessageOut>> call, Response<List<MessageOut>> res) {
                showLoading(false);
                if (!res.isSuccessful() || res.body() == null) {
                    toast("Failed to load messages (" + res.code() + ")");
                    return;
                }
                items.clear();
                items.addAll(res.body());
                adapter.notifyDataSetChanged();
                if (!items.isEmpty()) chatList.scrollToPosition(items.size() - 1);
            }

            @Override
            public void onFailure(Call<List<MessageOut>> call, Throwable t) {
                showLoading(false);
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private void sendMessage() {
        String text = chatInput.getText().toString().trim();
        if (text.isEmpty()) return;

        // Guard: must have a valid user id
        if (meUserId <= 0) {
            Toast.makeText(this, "Please sign in again (no user id).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optimistic local add
        chatInput.setText("");
        MessageOut local = new MessageOut();
        local.body = text;
        local.senderId = meUserId;
        local.conversationId = conversationId;
        local.createdAt = nowClock();

        items.add(local);
        int pos = items.size() - 1;
        adapter.notifyItemInserted(pos);
        chatList.scrollToPosition(pos);

        // Build payload: REQUIRED constructor (senderId, body)
        MessageSendIn payload = new MessageSendIn(meUserId, text);

        // Send using the SEND token
        sendCall = api.sendMessage(tokenChatSend, conversationId, payload);
        sendCall.enqueue(new Callback<MessageOut>() {
            @Override
            public void onResponse(Call<MessageOut> call, Response<MessageOut> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(chatActivity.this, "Send failed (" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    return; // keep optimistic message
                }
                // Replace optimistic with server copy (id/timestamp/etc.)
                items.set(pos, res.body());
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onFailure(Call<MessageOut> call, Throwable t) {
                Toast.makeText(chatActivity.this, "Send error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // keep optimistic message
            }
        });
    }

    // ===== UI helpers

    private void showLoading(boolean show) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String nowClock() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}