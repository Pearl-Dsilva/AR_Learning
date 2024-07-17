package com.sproj.arimagerecognizer;

import static com.sproj.arimagerecognizer.LanguageManager.availableLanguages;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private LanguageManager languageManager;
    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        languageManager = new LanguageManager(getApplication().getSharedPreferences(getString(R.string.language_selection), MODE_PRIVATE));

        TextView usernameTextView = findViewById(R.id.textViewUsername);
        TextView languageTextView = findViewById(R.id.languageTextView);
        Button buttonChangeLanguage = findViewById(R.id.buttonChangeLanguage);
        Button logoutButton = findViewById(R.id.buttonLogout);
        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("Username", "N/A");
        String language = sharedPreferences.getString("Language", "Language Not Chosen");
        ArrayList<String> availableLanguages = new ArrayList<>(LanguageManager.availableLanguages.keySet());

        usernameTextView.setText(username);
        languageTextView.setText(language);

        // If you want to perform actions when a new language is selected:
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        ArrayAdapter<String> languageAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableLanguages);
        languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdaptor);
        spinnerLanguage.setSelection(languageManager.getLanguage());

        buttonChangeLanguage.setOnClickListener(view -> {
            languageManager.languageSelected((int) spinnerLanguage.getSelectedItemId());
            Log.d(TAG, "onCreate: spinner language selected: "+availableLanguages.get(languageManager.getLanguage()));
            downloadModel(availableLanguages.get(languageManager.getLanguage()));
        });
//        //todo: [Pearl] apply same logic as home activity page
//        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // You could save the selected language preference here and refresh your activity to apply the language
//                boolean isDownloaded = languageManager.isModelDownloaded(availableLanguages.get(position));
//                if (isDownloaded) {
//                    languageManager.languageSelected(position);
//                } else {
//                    //TODO: show download confirmation (dialog OK, model is not available, should we download? space needed
//                    new MaterialAlertDialogBuilder(UserProfileActivity.this)
//                            .setIcon(R.drawable.baseline_info_24)
//                            .setTitle("Info")
//                            .setMessage("This language model is currently unavailable. Would you like to download it? Downloading will increase the app size and require an internet connection.")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //todo: if user chooses to download, show blocking dialog with indeterminate progress
//                                    dialog.dismiss();
//
//                                    new MaterialAlertDialogBuilder(UserProfileActivity.this)
//                                            .setIcon(R.mipmap.ic_launcher_foreground)
//                                            .setTitle("Downloading...")
//                                            .setMessage("Downloading language model.")
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                    //todo: when download successful, show user success dialog, set
//                                                    languageManager.modelDownloaded(availableLanguages.get(position));
//                                                    languageManager.languageSelected(position);
//                                                }
//                                            })
//                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                    // todo: when download fails, show user a failure dialog
//                                                    new MaterialAlertDialogBuilder(UserProfileActivity.this)
//                                                            .setIcon(R.mipmap.ic_launcher_foreground)
//                                                            .setTitle("Info")
//                                                            .setMessage("Download Failed.")
//                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                                @Override
//                                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                                    dialog.dismiss();
//                                                                }
//                                                            })
//                                                            .setNegativeButton("CANCEL", null)
//                                                            .show();
//                                                }
//                                            })
//                                            .create()
//                                            .show();
//                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    // todo: when download fails, show user a failure dialog
//                                    new MaterialAlertDialogBuilder(UserProfileActivity.this)
//                                            .setIcon(R.mipmap.ic_launcher_foreground)
//                                            .setTitle("Info")
//                                            .setMessage("Download Failed.")
//                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    dialog.dismiss();
//                                                }
//                                            })
//                                            .setNegativeButton("CANCEL", null)
//                                            .show();
//                                }
//                            })
//                            .create()
//                            .show();
//
//
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Another interface callback
//            }
//        });

        //logout of the account
        logoutButton.setOnClickListener(listener -> logoutUser());

        // Setup change password button, this should navigate to a different screen where the user can change their password
        buttonChangePassword.setOnClickListener(view -> showChangePasswordDialog());
    }

    void downloadModel(String languageSelected) {
        //start model download
        LayoutInflater inflater = LayoutInflater.from(UserProfileActivity.this);
        View customView = inflater.inflate(R.layout.model_downloading_alert, null);

        TextView message = customView.findViewById(R.id.model_message);
        Button dismissModel = customView.findViewById(R.id.dismiss_button);
        ProgressBar download_progress = customView.findViewById(R.id.download_progress);

        message.setText("Downloading language model.");
        androidx.appcompat.app.AlertDialog downloadingAlert = new MaterialAlertDialogBuilder(this).setIcon(R.mipmap.ic_launcher_foreground).setView(customView).create();

        dismissModel.setEnabled(false);
        downloadingAlert.show();
//        Log.d(TAG, "downloadModel: " + availableLanguages.get(languageManager.getLanguage()));


        new ModelDownloader().downloader(this, languageManager.getAvailableLanguage().get(languageSelected),
                (m) -> {
                    download_progress.setVisibility(View.GONE);
                    message.setText(m);
                    dismissModel.setText("DISMISS");
                    dismissModel.setOnClickListener(listener -> {
                        languageManager.setupCompleted();
                        downloadingAlert.dismiss();
                    });
                    dismissModel.setEnabled(true);
                }, (m) -> {
                    download_progress.setVisibility(View.GONE);
                    message.setText(m);
                    dismissModel.setText("RETRY");
                    dismissModel.setOnClickListener(listener -> {
                        downloadingAlert.dismiss();
                        downloadModel(languageSelected);
                    });
                    dismissModel.setEnabled(true);

                });
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
