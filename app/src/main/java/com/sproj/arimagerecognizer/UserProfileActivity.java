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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private LanguageManager languageManager;
    private TextView languageTextView;
    private static final String TAG = "UserProfileActivity";
    private final ArrayList<String> availableLanguages = new ArrayList<>(LanguageManager.availableLanguages.keySet());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        languageTextView = findViewById(R.id.languageTextView);
        languageManager = new LanguageManager(getApplication().getSharedPreferences(getString(R.string.language_selection), MODE_PRIVATE));

        TextView usernameTextView = findViewById(R.id.textViewUsername);
        Button buttonChangeLanguage = findViewById(R.id.buttonChangeLanguage);
        Button logoutButton = findViewById(R.id.buttonLogout);
        Button deleteButton = findViewById(R.id.buttonDeleteAccount);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        String language = sharedPreferences.getString("Language", availableLanguages.get(languageManager.getLanguage()));

        languageTextView.setText(language);
        usernameTextView.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail().trim());

        buttonChangeLanguage.setOnClickListener(view -> {
            selectLanguage();
        });

        //logout of the account
        logoutButton.setOnClickListener(listener -> logoutUser());

        //to delete account and their database
        deleteButton.setOnClickListener(listener -> deleteUserData());
    }

    void selectLanguage() {
        LayoutInflater inflater = LayoutInflater.from(UserProfileActivity.this);
        View customView = inflater.inflate(R.layout.dialog_custom, null);

        // Access views in the custom layout
        Spinner languageSpinner = customView.findViewById(R.id.languge_spinner);
        Button selectLanguageInitial = customView.findViewById(R.id.select_button);

        ArrayAdapter<String> languageAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableLanguages);
        languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdaptor);
        languageSpinner.setSelection(0); // default lang chinese

        AlertDialog initialLanguageSelectionDialog = new MaterialAlertDialogBuilder(UserProfileActivity.this).setIcon(R.mipmap.ic_launcher_foreground).setTitle("Language Selection").setMessage("Select language you wish to learn.").setView(customView).create();
        initialLanguageSelectionDialog.show();
        initialLanguageSelectionDialog.setCanceledOnTouchOutside(false);

        selectLanguageInitial.setOnClickListener(view -> {
            languageManager.languageSelected((int) languageSpinner.getSelectedItemId());
            initialLanguageSelectionDialog.dismiss();
            downloadModel(availableLanguages.get(languageManager.getLanguage()));
            languageTextView.setText(availableLanguages.get(languageManager.getLanguage()));
        });
        languageManager.getLanguage();
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

    public void deleteAllDocumentsInCollection(String collectionPath) {
        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int batchSize = 500;
                    List<WriteBatch> batches = new ArrayList<>();
                    WriteBatch currentBatch = db.batch();
                    int operationCount = 0;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (operationCount >= batchSize) {
                            batches.add(currentBatch);  // Store the current batch
                            currentBatch = db.batch();  // Create a new batch
                            operationCount = 0;         // Reset operation count
                        }
                        currentBatch.delete(document.getReference());
                        operationCount++;
                    }

                    // Add the last batch if it's not empty
                    if (operationCount > 0) {
                        batches.add(currentBatch);
                    }

                    // Commit all batches
                    for (WriteBatch batch : batches) {
                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("FirestoreBatchDelete", "Batch delete successful");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("FirestoreBatchDelete", "Batch delete failed: ", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreBatchDelete", "Error getting documents: ", e));
    }

    private void deleteUserData() {
        deleteAllDocumentsInCollection("labels/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "/recognisedLabels");
    }
}
