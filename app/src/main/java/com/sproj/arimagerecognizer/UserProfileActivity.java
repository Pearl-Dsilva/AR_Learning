package com.sproj.arimagerecognizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();

        TextView usernameTextView = findViewById(R.id.textViewUsername);
        TextView languageTextView = findViewById(R.id.languageTextView);

        Button logoutButton = findViewById(R.id.buttonLogout);

        // TODO: Implement loading languages from a on device database
//        language model download from mlkit
//        new ModelDownloader().downloader(TranslateLanguage.FRENCH, () -> {
//            Toast.makeText(this, "Downloaded", Toast.LENGTH_SHORT).show();
//        }, () -> {
//            Toast.makeText(this, "Not Downloaded", Toast.LENGTH_SHORT).show();
//        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("Username", "N/A");
        String language = sharedPreferences.getString("Language", "Language Not Chosen");

        usernameTextView.setText(username);
        languageTextView.setText(language);

        // If you want to perform actions when a new language is selected:
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        ArrayAdapter<String> languageAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<>(LanguageManager.availableLanguages.keySet()));
        languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdaptor);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // You could save the selected language preference here and refresh your activity to apply the language
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        logoutButton.setOnClickListener(listener -> logoutUser());

        // Setup change password button, this should navigate to a different screen where the user can change their password
        findViewById(R.id.buttonChangePassword).setOnClickListener(view -> showChangePasswordDialog());
    }

    private void logoutUser() {
        // Handle user logout logic here
        // This could involve clearing saved user credentials and navigating back to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mAuth.signOut();
        startActivity(intent);
        finish();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText editTextCurrentPassword = view.findViewById(R.id.editTextCurrentPassword);
        Button verifyIdentityButton = view.findViewById(R.id.verifyButton);
        EditText editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
        EditText editTextConfirmNewPassword = view.findViewById(R.id.editTextConfirmNewPassword);

        builder.setView(view)
                .setTitle("Change Password")
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = editTextCurrentPassword.getText().toString().trim();
                    String newPassword = editTextNewPassword.getText().toString().trim();
                    String confirmPassword = editTextConfirmNewPassword.getText().toString().trim();

                    if (currentPassword.isEmpty()) {
                        Toast.makeText(UserProfileActivity.this, "Please fill current password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //not working
                    verifyIdentityButton.setOnClickListener(newView -> {
                        String email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
                        assert email != null;
                        mAuth.signInWithEmailAndPassword(email, currentPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UserProfileActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Exception e = task.getException();
                                                assert e != null;
                                                Toast.makeText(UserProfileActivity.this, e.toString().trim(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    });


                    // Add validation and change password logic here
                    if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(UserProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(UserProfileActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: Implement the logic to change the password


                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
