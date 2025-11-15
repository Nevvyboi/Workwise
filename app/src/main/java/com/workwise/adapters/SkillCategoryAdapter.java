package com.workwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.workwise.R;
import com.workwise.models.SkillCategory;

import java.util.List;

public class SkillCategoryAdapter extends RecyclerView.Adapter<SkillCategoryAdapter.ViewHolder> {

    private List<SkillCategory> skills;

    public SkillCategoryAdapter(List<SkillCategory> skills) {
        this.skills = skills;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skill_category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SkillCategory skill = skills.get(position);
        holder.skillName.setText(skill.skillName);
        holder.skillLevel.setText(skill.level);
        holder.skillScore.setText(skill.score + "%");
        holder.progressFill.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                skill.score / 100f
        ));
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView skillName, skillLevel, skillScore;
        View progressFill;

        ViewHolder(View itemView) {
            super(itemView);
            skillName = itemView.findViewById(R.id.skillName);
            skillLevel = itemView.findViewById(R.id.skillLevel);
            skillScore = itemView.findViewById(R.id.skillScore);
            progressFill = itemView.findViewById(R.id.progressFill);
        }
    }
}
