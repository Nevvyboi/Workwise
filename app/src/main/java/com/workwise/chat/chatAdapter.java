package com.workwise.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MessageOut m = items.get(position);
        boolean isUserMessage = (m.senderId == meId);

        // Body + meta
        h.body.setText(m.body != null ? m.body : "");
        String who = isUserMessage ? "You" : "User " + m.senderId;
        h.meta.setText(String.format("%s • %s",
                who,
                m.createdAt != null ? m.createdAt : ""));

        // Align bubble depending on parent type (no unsafe casts)
        ViewGroup.LayoutParams lp = h.messageContainer.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) lp;
            p.gravity = isUserMessage ? Gravity.END : Gravity.START;
            h.messageContainer.setLayoutParams(p);
        } else if (lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) lp;
            p.gravity = isUserMessage ? Gravity.END : Gravity.START;
            h.messageContainer.setLayoutParams(p);
        } else if (lp instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) lp;
            // Clear both rules first
            p.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            p.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            p.addRule(isUserMessage ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            h.messageContainer.setLayoutParams(p);
        }

        // Bubble background (use your existing drawables)
        h.messageContainer.setBackgroundResource(
                isUserMessage ? R.drawable.chat_bubble_sent : R.drawable.chat_bubble_received
        );

        // Optional: different padding/margins could be set here if needed
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ViewGroup messageContainer;
        TextView body, meta;

        VH(@NonNull View v) {
            super(v);
            messageContainer = v.findViewById(R.id.messageContainer);
            body = v.findViewById(R.id.msgBody);
            meta = v.findViewById(R.id.msgMeta);
        }
    }
}
