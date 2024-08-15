package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private LanguageManager languageManager;
    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        languageManager = new LanguageManager(getApplication().getSharedPreferences(getString(R.string.language_selection), MODE_PRIVATE));

        TextView usernameTextView = findViewById(R.id.textViewUsername);
        TextView languageTextView = findViewById(R.id.languageTextView);
        Button buttonChangeLanguage = findViewById(R.id.buttonChangeLanguage);
        Button logoutButton = findViewById(R.id.buttonLogout);
        Button deleteButton = findViewById(R.id.buttonDeleteAccount);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("Username", "N/A");
        String language = sharedPreferences.getString("Language", "Language Not Chosen");
        ArrayList<String> availableLanguages = new ArrayList<>(LanguageManager.availableLanguages.keySet());

//        usernameTextView.setText(username);
        languageTextView.setText(language);
        usernameTextView.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail().trim());

        // If you want to perform actions when a new language is selected:
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        ArrayAdapter<String> languageAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableLanguages);
        languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdaptor);
        spinnerLanguage.setSelection(languageManager.getLanguage());
        languageTextView.setText(availableLanguages.get(languageManager.getLanguage()));

        buttonChangeLanguage.setOnClickListener(view -> {
            languageManager.languageSelected((int) spinnerLanguage.getSelectedItemId());
            Log.d(TAG, "onCreate: spinner language selected: "+availableLanguages.get(languageManager.getLanguage()));
            downloadModel(availableLanguages.get(languageManager.getLanguage()));
            languageTextView.setText(availableLanguages.get(languageManager.getLanguage()));

        });

        //logout of the account
        logoutButton.setOnClickListener(listener -> logoutUser());

        //to delete account and their database
        deleteButton.setOnClickListener(listener -> deleteUserAccount());
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
        downloadingAlert.setCanceledOnTouchOutside(false);
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

    private void deleteUserAccount(){
        FirebaseUser user = mAuth.getCurrentUser();
        CollectionReference transactionsRef = db.collection("labels/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "/recognisedLabels");
        CollectionReference collectionRef = db.collection("labels/" + user.getEmail() + "/recognisedLabels");

    }

}
