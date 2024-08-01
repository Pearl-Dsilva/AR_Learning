package com.sproj.arimagerecognizer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.sproj.arimagerecognizer.authentication.AuthenticationResult;
import com.sproj.arimagerecognizer.authentication.Validation;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View LoadingIndicatorLog;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        final EditText usernameInput = findViewById(R.id.editTextUsername);
        final EditText passwordInput = findViewById(R.id.editTextPassword);
        final TextView navigateToSignupPage = findViewById(R.id.textViewSignUp);
        final TextView forgotPassword = findViewById(R.id.forgotPass);
        final Button loginButton = findViewById(R.id.button_enter);
        LoadingIndicatorLog = findViewById(R.id.loadingIndicatorLog);

        navigateToSignupPage.setOnClickListener(this::onRegisterClick);
        forgotPassword.setOnClickListener(this::onForgotPasswordClick);

        loginButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            loginUser(username, password);
        });

//        new ModelDownloader().downloader(TranslateLanguage.FRENCH, () -> {
//            Toast.makeText(this, "Downloaded", Toast.LENGTH_SHORT).show();
//        }, () -> {
//            Toast.makeText(this, "Not Downloaded", Toast.LENGTH_SHORT).show();
//        });

    }

    public void loginUser(String email, String password) {
        if (!(validateEmail(email) && validatePassword(password)))
            return;

        LoadingIndicatorLog.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(success -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(failure -> Toast.makeText(LoginActivity.this, "Login Failed: " + failure.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCompleteListener(complete -> {
                    LoadingIndicatorLog.setVisibility(View.GONE);
                });
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

    public void onRegisterClick(View view) {
        // Intent to open the login activity
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish(); // Optional: if you want to close the registration screen after going to the login
    }

    public void onForgotPasswordClick(View view) {
        // Create an alert dialog to ask for the email
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void sendPasswordResetEmail(String email) {
        LoadingIndicatorLog.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LoadingIndicatorLog.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to send reset email", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}