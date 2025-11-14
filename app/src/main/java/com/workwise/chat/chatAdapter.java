package com.workwise.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.workwise.R;
import com.workwise.models.MessageOut;

import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.VH> {
    private final List<MessageOut> items;
    private final int meId;

    public chatAdapter(List<MessageOut> items, int meId) {
        this.items = items;
        this.meId = meId;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageOut message = items.get(position);

        holder.body.setText(message.body);

        // Set metadata (sender and time)
        String senderName = message.senderId == meId ? "You" : "User " + message.senderId;
        holder.meta.setText(String.format("%s • %s", senderName, message.createdAt));

        // Style based on who sent the message
        if (message.senderId == meId) {
            // My messages - align right with different background
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_sent);
            holder.body.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.meta.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.chat_meta_sent));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
            params.gravity = android.view.Gravity.END;
            holder.messageContainer.setLayoutParams(params);
        } else {
            // Other person's messages - align left
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_received);
            holder.body.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
            holder.meta.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.chat_meta_received));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
            params.gravity = android.view.Gravity.START;
            holder.messageContainer.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView body, meta;

        VH(View v) {
            super(v);
            messageContainer = v.findViewById(R.id.messageContainer);
            body = v.findViewById(R.id.msgBody);
            meta = v.findViewById(R.id.msgMeta);
        }
    }
}