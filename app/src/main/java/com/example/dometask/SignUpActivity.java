package com.example.dometask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Instância Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Pegar as variáveis que o usuário digitar / Botão de Registro
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);

        // Texto para ir à tela de login
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Se o usuário já estiver logado
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(SignUpActivity.this, "Bem-vindo de volta", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, MainActivity2.class));
        }

        // Quando o usuário clicar em Registrar-se
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Salvar email e senha na variável
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();

                // Se algum dos campos estiverem vazios
                if(user.isEmpty()) {
                    signupEmail.setError("O campo 'Email' não pode estar vazio");
                }
                if(pass.isEmpty()) {
                    signupPassword.setError("O campo 'Senha' não pode estar vazia");
                } else { // Caso contrário
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) { // Sucesso
                                Toast.makeText(SignUpActivity.this, "Registro Concluído!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            } else { // Erro
                                String errorMessage = "Falha ao Registrar: ";
                                Exception exception = task.getException();

                                if (exception instanceof FirebaseAuthWeakPasswordException) {
                                    errorMessage += "Sua senha deve ter pelo menos 6 caracteres!";
                                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage += "Email inválido!";
                                } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                    errorMessage += "Este email já está sendo usado por outra conta!";
                                } else {
                                    assert exception != null;
                                    errorMessage += "Erro desconhecido: " + exception.getMessage();
                                }

                                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        final EditText signupPasswordEditText = signupPassword; // findViewById(R.id.signup_password)

        // Icones usados
        final Drawable visibleIcon = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_24);
        final Drawable invisibleIcon = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_off_24);
        final Drawable lockIcon = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24);

        // Código para esconder a senha do usuário
        signupPasswordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_LEFT = 0;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (signupPasswordEditText.getRight() - signupPasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (signupPasswordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            signupPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            signupPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, invisibleIcon, null);
                        } else {
                            signupPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            signupPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, visibleIcon, null);
                        }
                        signupPasswordEditText.setSelection(signupPasswordEditText.length());
                        return true;
                    } else if (event.getRawX() <= (signupPasswordEditText.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        return true;
                    }
                }
                return false;
            }
        });

        // Enviar para tela de login, caso clicada
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

    }
}