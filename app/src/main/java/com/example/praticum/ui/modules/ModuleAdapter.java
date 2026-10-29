
package com.example.praticum.ui.modules;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.Course;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModuleAdapter
        extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    public interface OnModuleClickListener {
        void onModuleClick(Course module);
    }

    private final List<Course> modules = new ArrayList<>();
    private final OnModuleClickListener listener;

    public ModuleAdapter(OnModuleClickListener listener) {
        this.listener = listener;
    }

    public void setModules(List<Course> newModules) {
        modules.clear();

        if (newModules != null) {
            modules.addAll(newModules);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module, parent, false);

        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ModuleViewHolder holder,
            int position) {

        Course module = modules.get(position);

        holder.tvCode.setText(module.getCode());
        holder.tvName.setText(module.getName());
        holder.tvSemester.setText(
                "Semestre " + module.getSemester()
        );

        holder.tvCoefficient.setText(
                String.format(
                        Locale.getDefault(),
                        "Coef. %s",
                        module.getCoefficient()
                )
        );

        String description = module.getDescription();

        holder.tvDescription.setText(
                description == null || description.trim().isEmpty()
                        ? "Aucune description"
                        : description
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModuleClick(module);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCode;
        final TextView tvName;
        final TextView tvSemester;
        final TextView tvCoefficient;
        final TextView tvDescription;

        ModuleViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCode = itemView.findViewById(R.id.tvModuleCode);
            tvName = itemView.findViewById(R.id.tvModuleName);
            tvSemester = itemView.findViewById(R.id.tvModuleSemester);
            tvCoefficient = itemView.findViewById(R.id.tvModuleCoefficient);
            tvDescription = itemView.findViewById(R.id.tvModuleDescription);
        }
    }
}
