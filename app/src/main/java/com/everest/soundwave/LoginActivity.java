package com.everest.soundwave;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.everest.soundwave.auth.AuthManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AuthManager auth = AuthManager.get(this);
        if (auth.isLoggedIn()) {
            goToMain();
            return;
        }

        EditText username = findViewById(R.id.login_username);
        EditText pwd = findViewById(R.id.login_password);
        Button btnLogin = findViewById(R.id.btn_primary);
        Button btnRegister = findViewById(R.id.btn_secondary);

        btnLogin.setOnClickListener(v -> {
            String err = auth.login(
                    username.getText().toString(),
                    pwd.getText().toString());
            if (err == null) {
                goToMain();
            } else {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            boolean ok = auth.register(
                    username.getText().toString(),
                    pwd.getText().toString());
            if (ok) {
                Toast.makeText(this, R.string.login_registered, Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, R.string.login_register_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
