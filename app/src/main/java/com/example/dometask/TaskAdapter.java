package com.example.dometask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private DatabaseReference userTaskListRef;

    public TaskAdapter(List<Task> taskList, DatabaseReference userTaskListRef) {
        this.taskList = taskList;
        this.userTaskListRef = userTaskListRef;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskStatus.setText(task.getStatus() ? "Completada" : "Em aberto");
        holder.taskStatus.setBackgroundColor(task.getStatus() ? Color.rgb(25, 62, 1) : Color.rgb(101, 22, 22));

        holder.taskToggleStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.setStatus(!task.getStatus());
                notifyItemChanged(position);
                userTaskListRef.setValue(taskList);
            }
        });

        holder.taskDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(R.string.confirmDelete_dialog_message)
                        .setTitle(R.string.confirmDelete_dialog_title)
                        .setPositiveButton(R.string.confirmLogout, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                taskList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, taskList.size());
                                userTaskListRef.setValue(taskList);
                                Toast.makeText(v.getContext(), "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancelLogout, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDescription;
        TextView taskStatus;
        Button taskToggleStatusButton;
        Button taskDeleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDescription = itemView.findViewById(R.id.task_description);
            taskStatus = itemView.findViewById(R.id.task_status);
            taskToggleStatusButton = itemView.findViewById(R.id.task_toggle_status);
            taskDeleteButton = itemView.findViewById(R.id.task_delete);
        }
    }
}
