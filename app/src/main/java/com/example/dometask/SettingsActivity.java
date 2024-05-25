package com.example.dometask;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchPushNotifications;
    private SwitchCompat switchEmailNotifications;
    private static final String PREF_FILE_NAME = "my_preferences";
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Botão pra voltar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Se tiver um usuário, pegar o email dele e salvar no cache, caso não tenhamos perguntado as permissões, perguntar
        if (mUser != null) {
            String userEmail = mUser.getEmail();

            SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            boolean permissionsAsked = sharedPreferences.getBoolean("permissionsAsked", false);

            if (!permissionsAsked) {
                PermissionUtils.askPermissions(this, userEmail);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permissionsAsked", true);
                editor.apply();
            }
        }

        // Inicializar os switches
        switchPushNotifications = findViewById(R.id.switch_appnotif);
        switchEmailNotifications = findViewById(R.id.switch_emailnotif);

        // Carregar as preferências e atualizar os switches
        updateSwitches();

        // Adicionar os listeners pra atualizar as preferências quando o switch for selecionado
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> savePreferences());
        switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> savePreferences());

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Se o botão for clicado
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Salvar as configurações no cache
    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("push_notifications", switchPushNotifications.isChecked());
        editor.putBoolean("email_notifications", switchEmailNotifications.isChecked());
        editor.apply();
    }

    // Perguntar quais permissões o usuário quer
    // TODO: Implementar
    public void askPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_permission, null);
        builder.setView(dialogView);

        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        Button buttonConfirm = dialogView.findViewById(R.id.button_confirm);

        AlertDialog alertDialog = builder.create();

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("push_notifications", switchPushNotifications.isChecked());
            editor.putBoolean("email_notifications", switchEmailNotifications.isChecked());
            editor.apply();
            alertDialog.dismiss();

            // Atualizar os switches no SettingsActivity
            updateSwitches();
        });

        alertDialog.show();
    }

    // Atualizar os switches
    private void updateSwitches() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        boolean pushNotifications = sharedPreferences.getBoolean("push_notifications", false);
        boolean emailNotifications = sharedPreferences.getBoolean("email_notifications", false);

        switchPushNotifications.setChecked(pushNotifications);
        switchEmailNotifications.setChecked(emailNotifications);
    }
}
