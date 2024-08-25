package com.sproj.arimagerecognizer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sproj.arimagerecognizer.arhelper.ml.ARActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class HomeActivity extends AppCompatActivity {
    private final ArrayList<String> availableLanguages = new ArrayList<>(LanguageManager.availableLanguages.keySet());
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "HomeActivity";
    LanguageManager languageManager;
    private final int TOP_N = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CardView openARCamera = findViewById(R.id.cardViewAR);
        CardView buttonQuiz = findViewById(R.id.cardTest);
        CardView buttonStudy = findViewById(R.id.cardStudy);
        CardView buttonTop3Users = findViewById(R.id.cardViewTopUsers);

        ImageView imageViewSettings = findViewById(R.id.imageViewSettings);

        ImageView imageViewHelp = findViewById(R.id.imageViewHelp);

        languageManager = new LanguageManager(getApplication().getSharedPreferences(getString(R.string.language_selection), MODE_PRIVATE));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Set up the click listeners for each button
        openARCamera.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ARActivity.class);
            startActivity(intent);
        });

        buttonQuiz.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        buttonStudy.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
            startActivity(intent);
        });

        buttonTop3Users.setOnClickListener(view -> showTopUsersDialog());

        imageViewSettings.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        imageViewHelp.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        logAppStarted();
        selectStartingLanguage();


    }

    void logAppStarted() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, FirebaseAuth.getInstance().getUid());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    private void showTopUsersDialog() {
        LogEvent logEvent = new LogEvent(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
        logEvent.getTodayTop(false).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Integer> emailCountMap = new HashMap<>();

                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    String email = documentSnapshot.getString("email");
                    if (email != null) {
                        emailCountMap.put(email, emailCountMap.getOrDefault(email, 0) + 1);
                    }
                }

                List<Map.Entry<String, Integer>> topEmails = emailCountMap.entrySet().stream()
                        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                        .limit(TOP_N)
                        .collect(Collectors.toList());

                StringBuilder topUsersMessage = new StringBuilder("Top Users Today:\n\n");
                for (Map.Entry<String, Integer> entry : topEmails) {
                    String username = entry.getKey().split("@")[0];
                    topUsersMessage.append(username).append(" : ").append(entry.getValue()).append(" points\n");
//                    topUsersMessage.append(entry.getKey()).append(": ").append(entry.getValue()).append(" events\n");
                }

                if (topEmails.isEmpty()) {
                    topUsersMessage.append("No users have logged events today.");
                }

                // Show dialog
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Top Users")
                        .setIcon(R.mipmap.ic_launcher_foreground)
                        .setMessage(topUsersMessage.toString())
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                // Handle the error
                Log.e(TAG, "Error getting top users: ", task.getException());
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Error")
                        .setIcon(R.mipmap.ic_launcher_foreground)
                        .setMessage("Failed to retrieve top users. Please try again later.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    void selectStartingLanguage() {
        //Load LanguageManager
        boolean setup = languageManager.isSetupCompleted();
        Log.d(TAG, "selectStartingLanguage: " + setup);

        if (!setup) {
            LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
            View customView = inflater.inflate(R.layout.dialog_custom, null);

            // Access views in the custom layout
            Spinner languageSpinner = customView.findViewById(R.id.languge_spinner);
            Button selectLanguageInitial = customView.findViewById(R.id.select_button);

            ArrayAdapter<String> languageAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableLanguages);
            languageAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            languageSpinner.setAdapter(languageAdaptor);
            languageSpinner.setSelection(0); // default lang chinese


            AlertDialog initialLanguageSelectionDialog = new MaterialAlertDialogBuilder(HomeActivity.this).setIcon(R.mipmap.ic_launcher_foreground).setTitle("Language Selection").setMessage("Select language you wish to learn.").setView(customView).create();
            initialLanguageSelectionDialog.show();
            initialLanguageSelectionDialog.setCanceledOnTouchOutside(false);

            selectLanguageInitial.setOnClickListener(view -> {
                languageManager.languageSelected((int) languageSpinner.getSelectedItemId());
                initialLanguageSelectionDialog.dismiss();
                downloadModel();
            });
        }

    }

    @SuppressLint("SetTextI18n")
    void downloadModel() {
        //start model download
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View customView = inflater.inflate(R.layout.model_downloading_alert, null);

        TextView message = customView.findViewById(R.id.model_message);
        Button dismissModel = customView.findViewById(R.id.dismiss_button);
        ProgressBar download_progress = customView.findViewById(R.id.download_progress);

        message.setText("Downloading the language chosen by you.");
        AlertDialog downloadingAlert = new MaterialAlertDialogBuilder(this).setIcon(R.mipmap.ic_launcher_foreground).setView(customView).create();
        downloadingAlert.setCanceledOnTouchOutside(false);
        dismissModel.setEnabled(false);
        downloadingAlert.show();
        Log.d(TAG, "downloadModel: " + availableLanguages.get(languageManager.getLanguage()));


        new ModelDownloader().downloader(this, languageManager.getAvailableLanguage().get(availableLanguages.get(languageManager.getLanguage())),
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
                        downloadModel();
                    });
                    dismissModel.setEnabled(true);

                });
    }
}