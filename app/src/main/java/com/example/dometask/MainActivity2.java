package com.example.dometask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dometask.databinding.ActivityMain2Binding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private FirebaseAuth auth;

    private AppBarConfiguration mAppBarConfiguration;
    private List<RoomItem> roomList = new ArrayList<>();
private ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         binding = ActivityMain2Binding.inflate(getLayoutInflater());
         setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewRoomConfirmationDialog();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_help)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void settingsMenuXml(MenuItem item) {
        // TODO: Criar menu de config
        startActivity(new Intent(MainActivity2.this, SettingsActivity.class));
    }

    public void logoutMenuXml(MenuItem item) {
        showLogoutConfirmationDialog();
    }

    // Desconectar da conta do FireBase
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmLogout_dialog_message)
                .setTitle(R.string.confirmLogout_dialog_title)
                .setPositiveButton(R.string.confirmLogout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        auth.signOut();
                        startActivity(new Intent(MainActivity2.this, SignUpActivity.class));
                        Toast.makeText(MainActivity2.this, "Desconectado com sucesso", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancelLogout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // TODO: Criar/Entrar em uma nova sala
    private void showNewRoomConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.newRoom_dialog_message)
                .setTitle(R.string.roomAlertTitle_dialog_title)
                .setPositiveButton(R.string.newRoom, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        List<RoomItem> roomList = new ArrayList<>();

                        RoomItem newRoom = new RoomItem();
                        roomList.add(newRoom);

                        Toast.makeText(MainActivity2.this, "Sala criada(TODO: ID/Interface sala)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.joinRoom, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity2.this, "TODO: entrar com id da sala", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}