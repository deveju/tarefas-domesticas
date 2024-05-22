package com.example.dometask.ui.rooms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dometask.R;
import com.example.dometask.Task;
import com.example.dometask.TaskAdapter;
import com.example.dometask.databinding.FragmentRoomBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomFragment extends Fragment {

    private DatabaseReference mDatabase;
    private FragmentRoomBinding binding;
    private List<Task> taskList;
    private TaskAdapter adapter;
    private LayoutInflater inflater;
    private DatabaseReference userTaskListRef;
    private String taskListId = "defaultTaskListId";
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Usuário n logado
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userTaskListRef = mDatabase.child("users").child(userId).child("taskList").child(taskListId);

        RoomViewModel roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        binding = FragmentRoomBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerView
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, userTaskListRef);
        recyclerView.setAdapter(adapter);

        // Carregar a lista de tarefas do Firebase
        loadTaskListFromFirebase();

        // Botão para adicionar nova tarefa
        Button buttonAddTask = binding.buttonAddTask;
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        return root;
    }

    private void loadTaskListFromFirebase() {
        userTaskListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    taskList = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<Task>>() {});
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                    }
                } else {
                    taskList = new ArrayList<>();
                }

                adapter.setTaskList(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error loading tasks: " + databaseError.getMessage());
            }
        });
    }

    private void addNewTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Criar nova tarefa!");

        // Inflar a tela pro diálogo
        View dialogView = inflater.inflate(R.layout.dialog_new_task, null);
        builder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);

        // Botões do diálogo
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                // Verifique se os campos não estão vazios
                if (!title.isEmpty() && !description.isEmpty()) {
                    // Crie uma nova tarefa | OBS: o 'title' está escrito com a primeira letra em maiúsculo
                    Task newTask = new Task(title.substring(0, 1).toUpperCase() + title.substring(1), description, taskList.size() + 1, false);

                    // Adicione a nova tarefa à lista
                    taskList.add(newTask);

                    // Notifique o adapter que um item foi inserido
                    adapter.notifyItemInserted(taskList.size() - 1);

                    // Atualize a lista de tarefas no Firebase
                    userTaskListRef.setValue(taskList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Firebase", "Tarefa adicionada com sucesso!");
                                Toast.makeText(getContext(), "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("FirebaseError", "Erro ao adicionar a tarefa.");
                                Toast.makeText(getContext(), "Erro ao adicionar a tarefa.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // Caso os campos estejam vazios
                    Toast.makeText(getContext(), "Os campos não podem estar vazios!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
