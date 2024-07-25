package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.sproj.arimagerecognizer.arhelper.ml.ARActivity;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity {
    private final ArrayList<String> availableLanguages = new ArrayList<>(LanguageManager.availableLanguages.keySet());
    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "HomeActivity";
    LanguageManager languageManager;

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
            // TODO: Start the Quiz Activity
            Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        buttonStudy.setOnClickListener(view -> {
            // TODO: Start the Study Activity
            Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
            startActivity(intent);
        });

        imageViewSettings.setOnClickListener(view -> {
            // TODO: Show settings options
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        imageViewHelp.setOnClickListener(view -> {
            // TODO: Show help information
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
//        check if user first time, call isSetupCompleted, continue if false
//        show dialog to select language (first time)
//        save lang preference, call SetupCompleted
//        languageManager.modelDownloaded(availableLanguages.get(position));
//        languageManager.languageSelected(position);
    }

    void downloadModel() {
        //start model download
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View customView = inflater.inflate(R.layout.model_downloading_alert, null);

        TextView message = customView.findViewById(R.id.model_message);
        Button dismissModel = customView.findViewById(R.id.dismiss_button);
        ProgressBar download_progress = customView.findViewById(R.id.download_progress);

        message.setText("Downloading language model.");
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