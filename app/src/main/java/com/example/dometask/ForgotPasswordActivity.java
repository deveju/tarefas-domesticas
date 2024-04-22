package com.example.dometask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    // Variáveis do Firebase
    private FirebaseAuth auth;

    // Variáveis de elementos de interface
    private EditText forgotEmail;
    private Button forgotButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Atribuindo a instância da classe firebaseauth à variável firebase
        auth = FirebaseAuth.getInstance();

        // Atribuindo as views às suas variáveis
        forgotEmail = findViewById(R.id.forgot_email);
        forgotButton = findViewById(R.id.forgot_button);
        backButton = findViewById(R.id.back_button);

        // Ação do botão de enviar recuperação para o email
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Salvar o email na variável email
                String email = forgotEmail.getText().toString().trim();

                // Se a variável estiver vazia:
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Digite seu email!", Toast.LENGTH_SHORT).show();
                } else {
                    // Se não estiver, o email será enviado
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Em caso de email enviado sem erros
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Email de recuperação enviado!", Toast.LENGTH_SHORT).show();
                                        // Caso haja algum erro
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Falha ao enviar email de recuperação!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        // Ação do botão de voltar para página anterior
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}