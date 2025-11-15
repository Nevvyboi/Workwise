
package com.workwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.workwise.R;
import com.workwise.models.AssessmentHistory;

import java.util.List;

public class AssessmentHistoryAdapter extends RecyclerView.Adapter<AssessmentHistoryAdapter.VH> {

    private List<AssessmentHistory> data;

    public AssessmentHistoryAdapter(List<AssessmentHistory> data) {
        this.data = data;
    }

    public void update(List<AssessmentHistory> d) {
        this.data = d;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.assessment_history_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AssessmentHistory h = data.get(position);
        holder.date.setText(h.date);
        holder.category.setText(h.category);
        holder.score.setText(h.score + "%");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView date, category, score;
        VH(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.historyDate);
            category = itemView.findViewById(R.id.historyCategory);
            score = itemView.findViewById(R.id.historyScore);
        }
    }
}
