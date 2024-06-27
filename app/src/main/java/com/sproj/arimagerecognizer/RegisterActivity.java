package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sproj.arimagerecognizer.authentication.AuthenticationResult;
import com.sproj.arimagerecognizer.authentication.Validation;


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        final EditText usernameEditText = findViewById(R.id.editTextNewUsername);
        final EditText passwordEditText = findViewById(R.id.editTextNewPassword);
        final EditText confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        Button registerButton = findViewById(R.id.button_register_enter);

        // Set click listener for the register button
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            registerNewUser(username, password, confirmPassword);
        });
        mAuth = FirebaseAuth.getInstance();
    }

    private void registerNewUser(String email, String password, String confirmPassword) {

        if (!(validateEmail(email) && validatePassword(password) && validateConfirmPassword(password, confirmPassword)))
            return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(success -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(failure -> Toast.makeText(this, failure.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateEmail(String email) {
        AuthenticationResult result = Validation.emailValidator(email);
        if (!result.result) Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
        return result.result;
    }

    private boolean validatePassword(String password) {
        AuthenticationResult result = Validation.passwordValidator(password);
        Log.d(TAG, "validatePassword: " + result.message + " : " + result.result);
        if (!result.result) Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
        return result.result;
    }

    private boolean validateConfirmPassword(String password, String confirmPassword) {
        AuthenticationResult result = Validation.repeatPasswordValidator(password, confirmPassword);
        if (!result.result) Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
        return result.result;
    }

    public void onSignInClick(View view) {
        // Intent to open the login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: if you want to close the registration screen after going to the login
    }
}
