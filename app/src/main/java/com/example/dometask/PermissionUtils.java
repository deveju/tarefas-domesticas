package com.example.dometask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

public class PermissionUtils {

    private static final String PREF_FILE_NAME = "my_preferences";
    private static final String PREF_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String PREF_EMAIL_NOTIFICATIONS = "email_notifications";
    private static final String PREF_PERMISSIONS_ASKED = "permissionsAsked";
    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    public static void askPermissions(Activity activity, String userEmail) {
        // Diálogo para perguntar quais notificações o usuário quer receber
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_permission, null);
        builder.setView(dialogView);

        // Switch e botões de confirmar e cancelar permissões
        // TODO: Implementar
        SwitchCompat switchPushNotifications = dialogView.findViewById(R.id.switch_push_notifications);
        SwitchCompat switchEmailNotifications = dialogView.findViewById(R.id.switch_email_notifications);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        Button buttonConfirm = dialogView.findViewById(R.id.button_confirm);

        AlertDialog alertDialog = builder.create();

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            // Solicitar as permissões usando ActivityCompat
            // TODO: Implementar
            /*
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.PUSH_NOTIFICATIONS, Manifest.permission.EMAIL_NOTIFICATIONS},
                    PermissionActivity.REQUEST_CODE_PERMISSIONS);
            alertDialog.dismiss();
             */
        });

        alertDialog.show();
    }

    public static void handlePermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Context context) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Verificar se todas as permissões foram concedidas
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Todas as permissões foram concedidas
                savePermissions(context, true, true); // Salvar as permissões como concedidas
                updateSwitchesInActivity(context); // Atualizar os switches na UI
            } else {
                // Se alguma permissão foi negada
                // TODO: Implementar
            }
        }
    }

    // Salvar as permissões no cache
    private static void savePermissions(Context context, boolean pushNotifications, boolean emailNotifications) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_PUSH_NOTIFICATIONS, pushNotifications);
        editor.putBoolean(PREF_EMAIL_NOTIFICATIONS, emailNotifications);
        editor.putBoolean(PREF_PERMISSIONS_ASKED, true);
        editor.apply();
    }

    // Atuaizar os Switches na SettingsActivity de acordo com o que está no cache
    private static void updateSwitchesInActivity(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean pushNotifications = sharedPreferences.getBoolean(PREF_PUSH_NOTIFICATIONS, false);
        boolean emailNotifications = sharedPreferences.getBoolean(PREF_EMAIL_NOTIFICATIONS, false);

        if (context instanceof PermissionActivity) {
            ((PermissionActivity) context).updateSwitches(pushNotifications, emailNotifications);
        }
    }
}