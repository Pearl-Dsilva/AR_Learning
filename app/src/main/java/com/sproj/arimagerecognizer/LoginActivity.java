package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sproj.arimagerecognizer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.sproj.arimagerecognizer.authentication.AuthenticationResult;
import com.sproj.arimagerecognizer.authentication.Validation;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        final EditText usernameInput = findViewById(R.id.editTextUsername);
        final EditText passwordInput = findViewById(R.id.editTextPassword);
        final TextView navigateToSignupPage = findViewById(R.id.textViewSignUp);
        final Button loginButton = findViewById(R.id.button_enter);

        navigateToSignupPage.setOnClickListener(listener -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        loginButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            loginUser(username, password);
        });

    }

    public void loginUser(String email, String password) {
        if (!(validateEmail(email) && validatePassword(password)))
            return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(success -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(failure -> Toast.makeText(LoginActivity.this, "Login Failed: " + failure.getMessage(), Toast.LENGTH_LONG).show());
    }

    public boolean validateEmail(String email) {
        AuthenticationResult result = Validation.emailValidator(email);
        if (!result.result)
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
        return result.result;
    }

    public boolean validatePassword(String password) {
        AuthenticationResult result = Validation.passwordValidator(password);
        if (!result.result)
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
        return result.result;
    }

}
