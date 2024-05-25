package com.example.dometask;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PermissionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private SwitchCompat switchPushNotifications;
    private SwitchCompat switchEmailNotifications;
    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // Inicialização das instâncias e do usuário FIrebase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Switches push/email
        switchPushNotifications = findViewById(R.id.switch_push_notifications);
        switchEmailNotifications = findViewById(R.id.switch_email_notifications);

        // Botão não implementado de pedir as permissões novamente
        // TODO: Implementar
        Button btnRequestPermissions = findViewById(R.id.btn_request_permissions);
        btnRequestPermissions.setOnClickListener(v -> {
            if (mUser != null) {
                String userEmail = mUser.getEmail();
                askPermissions(userEmail);
            }
        });

        updateSwitchesFromPreferences();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSIONS) {
            // Verificar se todas as permissões foram selecionadas
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Todas as permissões foram selecionadas
                savePermissions(true, true); // Salvar as permissões como selecionadas
                updateSwitchesFromPreferences(); // Atualizar os switches na UI
            } else {
                // Alguma permissão foi negada
            }
        }
    }
    private void askPermissions(String userEmail) {
        PermissionUtils.askPermissions(this, userEmail);
    }

    // Salvar os booleans de permissão no cache
    private void savePermissions(boolean pushNotifications, boolean emailNotifications) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("push_notifications", pushNotifications);
        editor.putBoolean("email_notifications", emailNotifications);
        editor.putBoolean("permissionsAsked", true);
        editor.apply();
    }

    // Mudar os switches na aba de configurações
    public void updateSwitches(boolean pushNotifications, boolean emailNotifications) {
        switchPushNotifications.setChecked(pushNotifications);
        switchEmailNotifications.setChecked(emailNotifications);
    }

    // Atualizar os switches se mudar o da configuração
    private void updateSwitchesFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        boolean pushNotifications = sharedPreferences.getBoolean("push_notifications", false);
        boolean emailNotifications = sharedPreferences.getBoolean("email_notifications", false);
        updateSwitches(pushNotifications, emailNotifications);
    }
}
