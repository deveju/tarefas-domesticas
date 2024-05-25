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
    private DatabaseReference roomTaskListRef;
    private String roomId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;

        Bundle args = getArguments();
        if (args != null) {
            roomId = args.getString("roomId");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("O ID da sala deve ser disponibilizado!");
        }

        // Instâncias Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        roomTaskListRef = mDatabase.child("rooms").child(roomId).child("tasks");

        // ViewModel
        RoomViewModel roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        // Binding
        binding = FragmentRoomBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerView
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ArrayList da lista de tarefas e inicialização do adapter das tarefas
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, roomTaskListRef);
        recyclerView.setAdapter(adapter);

        // Carregar a lista de tarefas do Firebase
        loadTaskListFromFirebase();

        Button buttonAddTask = binding.buttonAddTask;
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        return root;
    }

    // Nova instância do Fragmento de sala
    public static RoomFragment newInstance(String roomId) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putString("roomId", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    // Carregar as tarefas do Firebase
    private void loadTaskListFromFirebase() {
        roomTaskListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<ArrayList<Task>> t = new GenericTypeIndicator<ArrayList<Task>>() {};
                    taskList = dataSnapshot.getValue(t);
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                    }
                    // Adicionar a lista à sala
                    adapter.setTaskList(taskList);
                } else {
                    taskList = new ArrayList<>();
                }
                adapter.setTaskList(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Erro ao carregar as tarefas: " + databaseError.getMessage());
            }
        });
    }

    // Criar nova tarefa
    private void addNewTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Criar nova tarefa: ");

        View dialogView = inflater.inflate(R.layout.dialog_new_task, null);
        builder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);

        builder.setPositiveButton("Criar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                if (!title.isEmpty() && !description.isEmpty()) {
                    Task newTask = new Task(title.substring(0, 1).toUpperCase() + title.substring(1), description, taskList.size() + 1, false);
                    taskList.add(newTask);
                    adapter.notifyItemInserted(taskList.size() - 1);
                    roomTaskListRef.setValue(taskList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Firebase", "Tarefa adicionada com sucesso!");
                                Toast.makeText(getContext(), "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("FirebaseError", "Erro ao adicionar tarefa!");
                                Toast.makeText(getContext(), "Erro ao adicionar tarefa!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
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
