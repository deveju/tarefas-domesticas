package com.example.dometask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    // Variáveis do firebase
    private FirebaseAuth auth;

    // Variáveis de elementos de interface
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private TextView forgotPasswordRedirectText;
    private Button loginButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instanciando as variáveis
        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signup_redirect_textview);
        forgotPasswordRedirectText = findViewById(R.id.forgotpass_redirect_textview);

        // Ação do botão de logar
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Salvar as informações em variáveis
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();

                // Se os campos não  estiverem vazios e o email estiver no padrão correto
                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if(!pass.isEmpty()) {
                        // Em caso de sucesso
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity2.class));
                                        finish();
                                    }
                                    // Em caso de falha
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Alguma informação de login está incorreta!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        // Se o campo senha estiver vazio
                    } else {
                        loginPassword.setError("O campo 'Senha' não pode estar vazio!");
                    }
                    // Se o campo email estiver vazio
                } else if (email.isEmpty()) {
                    loginEmail.setError("O campo 'Email' não pode estar vazio!");
                } else {
                    // Se o email estiver num padrão incorreto
                    loginEmail.setError("Digite um Email válido!");
                }
            }
        });

        // Função para esconder a senha
        final EditText loginPasswordEditText = loginPassword; // findViewById(R.id.login_password)
        final Drawable visibleIcon = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_24);
        final Drawable invisibleIcon = ContextCompat.getDrawable(this, R.drawable.baseline_visibility_off_24);
        final Drawable lockIcon = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24);

        loginPasswordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_LEFT = 0;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (loginPasswordEditText.getRight() - loginPasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (loginPasswordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            loginPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            loginPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, invisibleIcon, null);
                        } else {
                            loginPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            loginPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(lockIcon, null, visibleIcon, null);
                        }
                        loginPasswordEditText.setSelection(loginPasswordEditText.length());
                        return true;
                    } else if (event.getRawX() <= (loginPasswordEditText.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        return true;
                    }
                }
                return false;
            }
        });

        // Função para ir para a tela de registro
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        // Função para ir para a tela de recuperação
        forgotPasswordRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

    }
}