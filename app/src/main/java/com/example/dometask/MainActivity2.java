package com.example.dometask;

import com.example.dometask.ui.rooms.RoomFragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dometask.databinding.ActivityMain2Binding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity2 extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMain2Binding binding;
    private static final String PREFS_FILE_NAME = "RoomPrefs";
    private static final String PREF_CURRENT_ROOM_ID = "currentRoomId";
    private static final String PREF_CURRENT_ROOM_NAME = "currentRoomName";
    private static final String PREF_FILE_NAME = "my_preferences";
    private boolean permissionsAsked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Iniciar o banco e autenticação do Firebase
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = auth.getCurrentUser();

        // Se o usuário existir
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            permissionsAsked = sharedPreferences.getBoolean("permissionsAsked", false);
            if (!permissionsAsked) { // Se não perguntou as permissões, perguntar e salvar no dispositivo
                PermissionUtils.askPermissions(this, userEmail);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permissionsAsked", true);
                editor.apply();
            }
        }

        // ActionBar para navegação do usuário
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewRoomConfirmationDialog();
            }
        });

        // Drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_help)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Adicionar as salas que o usuário possui no menu
        addStoredRoomsToMenu();
    }

    // Quando criar o menu, exibir no MainActivity2
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    // Implementação NavController
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Ir para a atividade do menu de configurações
    public void settingsMenuXml(MenuItem item) {
        startActivity(new Intent(MainActivity2.this, SettingsActivity.class));
    }

    // Função do Logout no menu
    public void logoutMenuXml(MenuItem item) {
        showLogoutConfirmationDialog();
    }

    // Confirmação de logout
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmLogout_dialog_message)
                .setTitle(R.string.confirmLogout_dialog_title)
                .setPositiveButton(R.string.confirmLogout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Desconectar e enviar o usuário para a atividade de registro novamente
                        auth.signOut();
                        startActivity(new Intent(MainActivity2.this, SignUpActivity.class));
                        Toast.makeText(MainActivity2.this, "Desconectado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancelLogout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNewRoomConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.newRoom_dialog_message)
                .setTitle(R.string.roomAlertTitle_dialog_title)
                .setPositiveButton(R.string.newRoom, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Criar nova sala
                        createNewRoom();
                    }
                })
                .setNegativeButton(R.string.joinRoom, new DialogInterface.OnClickListener() {
                    // Entrar em uma sala
                    public void onClick(DialogInterface dialog, int id) {
                        joinExistingRoom();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Criação de nova sala
    private void createNewRoom() {
        // Diálogo da criação, que perdirá um nome para a sala ao usuário
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
        builder.setTitle("Digite o nome da sala: ");

        final EditText input = new EditText(MainActivity2.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Lista vazia de tarefas
        List<Task> emptyTaskList = new ArrayList<>();

        // Ao clicar em criar
        builder.setPositiveButton("Criar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Pegar o nome da sala em uma variável e pegar o usuário
                String roomName = input.getText().toString().trim();
                FirebaseUser currentUser = auth.getCurrentUser();
                if (!TextUtils.isEmpty(roomName) && currentUser != null) {
                    try {
                        // Fazer push no banco de dados usando o nome, id da sala e id do usuário, assim como a arraylist de tarefas da sala
                        DatabaseReference newRoomRef = mDatabase.child("rooms").push();
                        String roomId = newRoomRef.getKey();
                        String creatorId = currentUser.getUid();
                        newRoomRef.setValue(new Room(roomId, roomName, creatorId, emptyTaskList)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Criar sala e abrir na tela usando o RoomFragment
                                    Toast.makeText(MainActivity2.this, "Sala criada com sucesso!", Toast.LENGTH_SHORT).show();
                                    openRoom(roomId, roomName);

                                    // Salvar sala no dispositivo e adicionar ela ao menu
                                    storeCurrentRoom(roomId, roomName, false);
                                    addStoredRoomsToMenu(); // Atualizar menu
                                } else {
                                    Toast.makeText(MainActivity2.this, "Erro ao criar sala", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        // Erros ao criar a sala
                    } catch (Exception e) {
                        Log.e("CreateRoomError", "Erro ao criar sala", e);
                        Toast.makeText(MainActivity2.this, "Erro ao criar sala: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity2.this, "Digite um nome para a sala!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Entrar numa sala já existente usando o ID
    private void joinExistingRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Entre com o ID da sala");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Entrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomId = input.getText().toString().trim();
                // Se o campo de ID não estiver vazio, procurar sala, caso exista, entrar
                if (!roomId.isEmpty()) {
                    checkRoomExistsAndJoin(roomId);
                } else {
                    Toast.makeText(MainActivity2.this, "O ID da sala não pode estar vazio!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cencelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Checar se a sala existe e, caso exista, entrar
    private void checkRoomExistsAndJoin(String roomId) {
        mDatabase.child("rooms").child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null && room.getRoomName() != null) {
                        String roomName = room.getRoomName();
                        openRoom(roomId, roomName);
                        storeCurrentRoom(roomId, roomName, false);
                        addStoredRoomsToMenu(); // Refresh menu
                    } else {
                        Toast.makeText(MainActivity2.this, "Os dados da sala são inválidos!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity2.this, "O ID da sala não existe!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity2.this, "Falha ao entrar na sala: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para excluir a sala
    private void deleteRoom(String roomId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        mDatabase.child("rooms").child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Room room = dataSnapshot.getValue(Room.class);
                if (room != null && room.getCreatorId().equals(userId)) {
                    mDatabase.child("rooms").child(roomId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity2.this, "Sala deletada com sucesso!", Toast.LENGTH_SHORT).show();
                                storeCurrentRoom(roomId, null, true);
                                addStoredRoomsToMenu(); // Atualizar o menu
                            } else {
                                Toast.makeText(MainActivity2.this, "Falha ao deletar sala!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity2.this, "Você não tem permissões para deletar esta sala!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity2.this, "Erro ao ver sala: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Guardar sala no cache
    private void storeCurrentRoom(String roomId, String roomName, boolean isDeleting) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isDeleting) {
            editor.remove(roomId);
        } else {
            editor.putString(PREF_CURRENT_ROOM_ID, roomId);
            editor.putString(PREF_CURRENT_ROOM_NAME, roomName);
            if (!sharedPreferences.contains(roomId)) {
                editor.putString(roomId, roomName);
            }
        }
        editor.apply();
    }

    // Métodos descontinuados para pegar o ID e Nome da sala
    private String getCurrentRoomId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_CURRENT_ROOM_ID, null);
    }

    private String getCurrentRoomName(String roomId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(roomId, null);
    }

    // Abrir a sala
    private void openRoom(String roomId, String roomName) {
        // Ir para o menu da sala
        navigateToRoomFragment(roomId);

        // Guardar o ID da sala atual
        storeCurrentRoom(roomId, roomName, false);
    }

    // Adicionar as salas salvas ao menu
    private void addStoredRoomsToMenu() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView == null) {
            return;
        }
        Menu menu = navigationView.getMenu();
        if (menu == null) {
            return;
        }
        menu.clear(); // Limpar o menu

        // Adicionar o botão 'Ajuda' primeiro
        menu.add(Menu.NONE, R.id.nav_help, Menu.NONE, "Ajuda")
                .setIcon(R.drawable.baseline_help_center_24)
                .setOnMenuItemClickListener(menuItem -> {
                    /*
                    NavController navController = Navigation.findNavController(MainActivity2.this, R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_help);
                    return true;
                     */
                    startActivity(new Intent(MainActivity2.this, HelpActivity.class));
                    return true;
                });

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String currentUserId = currentUser.getUid();

        // Contagem das operações do db
        AtomicInteger counter = new AtomicInteger(allEntries.size() - 2); // Exclude special keys

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String roomId = entry.getKey();
            if (roomId.equals(PREF_CURRENT_ROOM_ID) || roomId.equals(PREF_CURRENT_ROOM_NAME)) {
                continue; // Ignore special keys
            }
            String roomName = entry.getValue().toString();
            SubMenu roomMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "Sala: " + roomName);

            // Botão das ações da sala no menu
            roomMenu.add(Menu.NONE, roomId.hashCode(), Menu.NONE, "Visualizar Atividades").setIcon(R.drawable.baseline_visibility_24);
            roomMenu.add(Menu.NONE, roomId.hashCode() + 1, Menu.NONE, "Copiar Código").setIcon(R.drawable.baseline_content_copy_24);
            roomMenu.add(Menu.NONE, roomId.hashCode() + 2, Menu.NONE, "Deletar Sala").setIcon(R.drawable.baseline_delete_24);

            // Botão "Visualizar Atividades"
            roomMenu.getItem(0).setOnMenuItemClickListener(menuItem -> {
                navigateToRoomFragment(roomId);
                return true;
            });

            // Botão "Copiar Código"
            roomMenu.getItem(1).setOnMenuItemClickListener(menuItem -> {
                copyToClipboard(roomId);
                return true;
            });

            // Botão "Deletar Sala"
            roomMenu.getItem(2).setOnMenuItemClickListener(menuItem -> {
                showDeleteRoomConfirmationDialog(roomId);
                return true;
            });

            // Puxar tarefas associadas à sala no Firebase
            DatabaseReference roomRef = mDatabase.child("rooms").child(roomId);
            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null) {
                        List<Task> tasks = room.getTasks();
                        if (tasks != null) {
                            for (Task task : tasks) {
                                // Adicionar título das tarefas ao menu, embaixo da opção de deletar sala ( Tirei pq tava feio )
                                // roomMenu.add(Menu.NONE, task.getId(), Menu.NONE, task.getTitle());
                            }
                        }
                    }
                    // Update the menu
                    navigationView.invalidate();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity2.this, "Erro na obtenção dados: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    // Atualizar o menu
                    navigationView.invalidate();
                }
            });
        }

    }

    // Ao selecionar alguma opção do menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_help);
            return true;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String roomId = entry.getKey();
            if (id == roomId.hashCode()) {
                // Abrir sala
                openRoom(roomId, entry.getValue().toString());
                return true;
            } else if (id == (roomId.hashCode() + 1)) {
                // Deletar sala
                showDeleteRoomConfirmationDialog(roomId);
                return true;
            } else if (id == (roomId.hashCode() + 2)) {
                // Copiar o código da sala
                copyToClipboard(roomId);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Mostrar o diálogo de confirmação para excluir a sala
    private void showDeleteRoomConfirmationDialog(String roomId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmDeleteRoom_dialog_message)
                .setTitle(R.string.confirmDeleteRoom_dialog_title)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteRoom(roomId);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Enviar o usuário para a tela da sala
    private void navigateToRoomFragment(String roomId) {
        RoomFragment fragment = RoomFragment.newInstance(roomId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Copiar código para a área de transferência
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Código da sala: ", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Código copiado para área de transferência", Toast.LENGTH_SHORT).show();
    }
}